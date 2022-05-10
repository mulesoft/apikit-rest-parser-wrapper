/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.ValidatorAware;
import amf.client.validate.PayloadValidator;
import amf.client.validate.ValidationReport;
import org.json.simple.JSONValue;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.amf.impl.util.LazyValue;

import static org.mule.amf.impl.model.MediaType.APPLICATION_JSON;
import static org.mule.amf.impl.model.MediaType.APPLICATION_YAML;

class JsonParameterValidationStrategy extends ValidationStrategy {

  private final LazyValue<PayloadValidator> jsonValidator =
      new LazyValue<>(() -> schema.payloadValidator(APPLICATION_JSON)
          .orElseThrow(() -> new ParserException(APPLICATION_JSON + " validator not found for shape " + schema)));

  private final LazyValue<ValidationReport> nullValidationReport = new LazyValue<>(() -> {
    final PayloadValidator yamlPayloadValidator = schema.payloadValidator(APPLICATION_YAML)
        .orElseThrow(() -> new ParserException(APPLICATION_YAML + " validator not found for shape " + schema));

    return yamlPayloadValidator.syncValidate(APPLICATION_YAML, "null");
  });

  public JsonParameterValidationStrategy(ValidatorAware validatorAware, boolean schemaNeedsQuotes) {
    super(validatorAware, schemaNeedsQuotes);
  }

  @Override
  public boolean valueNeedQuotes(String value) {
    return schemaNeedsQuotes;
  }

  @Override
  public boolean needsPreProcess(String value) {
    return true;
  }

  @Override
  public ValidationReport validate(String value) {
    if (value == null) {
      return nullValidationReport.get();
    }
    return jsonValidator.get().syncValidate(APPLICATION_JSON, value);
  }


  @Override
  public String escapeCharsInValue(String value) {
    return JSONValue.escape(value);
  }

  @Override
  public String removeLeadingZeros(String value) {
    if (value == null || !value.startsWith("0")) {
      return value;
    }

    int indexOfLastLeadingZero = 0;
    for (; indexOfLastLeadingZero + 1 < value.length(); indexOfLastLeadingZero++) {
      char next = value.charAt(indexOfLastLeadingZero);

      if (next == '.') {// '0.' should be valid
        indexOfLastLeadingZero = indexOfLastLeadingZero - 1;
        break;
      }
      if (next != '0') {
        break;
      }
    }

    return value.substring(indexOfLastLeadingZero);
  }

}
