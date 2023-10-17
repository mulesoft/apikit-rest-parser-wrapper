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
import org.mule.amf.impl.util.LazyValue;

import static org.mule.amf.impl.model.MediaType.APPLICATION_JSON;
import static org.mule.amf.impl.model.MediaType.APPLICATION_YAML;

class JsonParameterValidationStrategy extends ValidationStrategy {

  private LazyValue<AMFShapePayloadValidator> jsonValidator;
  private LazyValue<AMFValidationReport> nullValidationReport;

  public JsonParameterValidationStrategy(AMFElementClient client, AnyShape anyShape, boolean schemaNeedsQuotes) {
    super(schemaNeedsQuotes);
    this.jsonValidator =
        new LazyValue<>(() -> client.payloadValidatorFor(anyShape, APPLICATION_JSON, ValidationMode.StrictValidationMode()));
    this.nullValidationReport = new LazyValue<>(() -> {
      AMFShapePayloadValidator yamlPayloadValidator =
          client.payloadValidatorFor(anyShape, APPLICATION_YAML, ValidationMode.StrictValidationMode());
      return yamlPayloadValidator.syncValidate("null");
    });
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
  public AMFValidationReport validate(String value) {
    if (value == null) {
      return nullValidationReport.get();
    }
    return jsonValidator.get().syncValidate(value);
  }


  @Override
  public String escapeCharsInValue(String value) {
    StringBuffer buf = new StringBuffer(value.length());
    value.codePoints().forEachOrdered(codepoint -> {
      // See https://www.json.org/json-en.html for the reference on how strings are encoded
      switch (codepoint) {
        case '"':
          buf.append("\\\"");
          break;
        case '\\':
          buf.append("\\\\");
          break;
        case '\b':
          buf.append("\\b");
          break;
        case '\f':
          buf.append("\\f");
          break;
        case '\n':
          buf.append("\\n");
          break;
        case '\r':
          buf.append("\\r");
          break;
        case '\t':
          buf.append("\\t");
          break;
        default:
          if (Character.isISOControl(codepoint)) {
            final char[] intoHex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
            buf.append("\\u");
            buf.append(intoHex[(codepoint >> 24) & 0xFF]);
            buf.append(intoHex[(codepoint >> 16) & 0xFF]);
            buf.append(intoHex[(codepoint >> 8) & 0xFF]);
            buf.append(intoHex[(codepoint >> 0) & 0xFF]);
          } else {
            // Any codepoint except " or \ or control characters
            buf.appendCodePoint(codepoint);
          }
          break;
      }
    });
    return buf.toString();
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
