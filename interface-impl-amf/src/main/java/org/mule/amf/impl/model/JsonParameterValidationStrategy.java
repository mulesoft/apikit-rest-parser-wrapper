/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.apicontract.client.platform.AMFElementClient;
import amf.core.client.common.validation.ValidationMode;
import amf.core.client.platform.validation.AMFValidationReport;
import amf.core.client.platform.validation.payload.AMFShapePayloadValidator;
import amf.shapes.client.platform.model.domain.AnyShape;
import org.json.simple.JSONValue;
import org.mule.amf.impl.util.LazyValue;

import static org.mule.amf.impl.model.MediaType.APPLICATION_JSON;
import static org.mule.amf.impl.model.MediaType.APPLICATION_YAML;
import static org.mule.amf.impl.model.ParameterImpl.quote;

class JsonParameterValidationStrategy implements ParameterValidationStrategy {

  private final boolean needsCharsEscaping;
  private AMFElementClient client;
  private AnyShape anyShape;

  private final LazyValue<AMFShapePayloadValidator> jsonValidator =
      new LazyValue<>(() -> client.payloadValidatorFor(anyShape, APPLICATION_JSON, ValidationMode.StrictValidationMode()));

  private final LazyValue<AMFValidationReport> nullValidationReport = new LazyValue<>(() -> {
    AMFShapePayloadValidator yamlPayloadValidator =
        client.payloadValidatorFor(anyShape, APPLICATION_YAML, ValidationMode.StrictValidationMode());
    return yamlPayloadValidator.syncValidate("null");
  });

  JsonParameterValidationStrategy(AnyShape anyShape, AMFElementClient client, boolean needsCharsEscaping) {
    this.anyShape = anyShape;
    this.needsCharsEscaping = needsCharsEscaping;
    this.client = client;
  }

  public AMFValidationReport validate(String value) {
    if (value == null) {
      return nullValidationReport.get();
    }

    return jsonValidator.get().syncValidate(getPayload(value));
  }

  private String getPayload(String value) {
    if (needsCharsEscaping && value.startsWith("\"")) {
      return quote(JSONValue.escape(value.substring(1, value.length() - 1)));
    }
    return removeLeadingZeros(value);
  }

  private String removeLeadingZeros(String value) {
    if (!value.startsWith("0")) {
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
