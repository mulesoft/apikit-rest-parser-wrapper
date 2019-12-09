/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.body;

import static com.github.fge.jsonschema.core.report.LogLevel.ERROR;
import static com.github.fge.jsonschema.core.report.LogLevel.WARNING;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import org.mule.validation.exception.BadRequestException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import org.apache.commons.lang.StringUtils;
import org.mule.runtime.api.metadata.TypedValue;

public class RestJsonSchemaValidator extends BodyAsStringValidator implements BodyValidator {

  private static final String JSON_SCHEMA_FAIL_ON_WARNING_KEY = "raml.json_schema.fail_on_warning";
  private static final String JSON_STRICT_DUPLICATE_DETECTION_PROPERTY = "yagi.json_duplicate_keys_detection";

  private final JsonSchema jsonSchema;


  public RestJsonSchemaValidator(JsonSchema jsonSchema) {
    this.jsonSchema = jsonSchema;
  }

  @Override
  public TypedValue validate(String charset, TypedValue payload) throws BadRequestException {
    validate(getPayloadAsString(payload.getValue(), charset));
    return payload;
  }

  private void validate(String payload) throws BadRequestException {

    if (jsonSchema != null) {
      JsonNode data;
      ProcessingReport report;

      try {
        boolean isEmpty = StringUtils.isEmpty(payload);
        data = parseJson(new StringReader(isEmpty ? "null" : payload));
        report = jsonSchema.validate(data, true);

      } catch (IOException | ProcessingException e) {
        throw new BadRequestException(e.getMessage());
      }

      Iterator<ProcessingMessage> iterator = report.iterator();
      final StringBuilder messageBuilder = new StringBuilder();

      while (iterator.hasNext()) {
        ProcessingMessage next = iterator.next();
        LogLevel logLevel = next.getLogLevel();
        String logMessage = next.toString();

        boolean failOnWarning = Boolean.valueOf(
                                                System.getProperty(JSON_SCHEMA_FAIL_ON_WARNING_KEY, "false"));

        if (logLevel.equals(ERROR) || (logLevel.equals(WARNING) && failOnWarning)) {
          messageBuilder.append(logMessage).append("\n");
        }
      }

      if (messageBuilder.length() > 0) {
        throw new BadRequestException(messageBuilder.toString());
      }
    }
  }

  private JsonNode parseJson(Reader reader) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.disableDefaultTyping();
    mapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, getSystemPropValue());
    return mapper.readValue(reader, JsonNode.class);
  }

  private boolean getSystemPropValue() {
    return Boolean.valueOf(System.getProperty(JSON_STRICT_DUPLICATE_DETECTION_PROPERTY, "true"));
  }
}
