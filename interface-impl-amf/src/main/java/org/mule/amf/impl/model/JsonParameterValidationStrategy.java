/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.AnyShape;
import amf.client.validate.PayloadValidator;
import amf.client.validate.ValidationReport;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.amf.impl.util.LazyValue;

import static org.mule.amf.impl.model.MediaType.APPLICATION_JSON;
import static org.mule.amf.impl.model.MediaType.APPLICATION_YAML;
import static org.mule.amf.impl.model.ParameterImpl.quote;

class JsonParameterValidationStrategy implements ParameterValidationStrategy {
  private final boolean needsQuotes;
  private final boolean isBoolean;
  private AnyShape anyShape;

  private final LazyValue<PayloadValidator> jsonValidator = new LazyValue<>(() -> anyShape.payloadValidator(APPLICATION_JSON)
          .orElseThrow(() -> new ParserException(APPLICATION_JSON + " validator not found for shape " + anyShape)));

  private final LazyValue<ValidationReport> nullValidationReport = new LazyValue<>(() -> {
    final PayloadValidator yamlPayloadValidator = anyShape.payloadValidator(APPLICATION_YAML)
            .orElseThrow(() -> new ParserException(APPLICATION_YAML + " validator not found for shape " + anyShape));

    return yamlPayloadValidator.syncValidate(APPLICATION_YAML, "null");
  });

  JsonParameterValidationStrategy(AnyShape anyShape, boolean needsQuotes, boolean isBoolean){
    this.anyShape = anyShape;
    this.needsQuotes = needsQuotes;
    this.isBoolean = isBoolean;
  }

  public ValidationReport validate(String value) {
    if(value == null){
      return nullValidationReport.get();
    }

    return jsonValidator.get().syncValidate(APPLICATION_JSON, getPayload(value));
  }

  private String getPayload(String value) {
    if(needsQuotes)
      return quote(value.replaceAll("\"", "\\\\\""));

    if(isBoolean)
      return value;

    return removeLeadingZeros(value);
  }

  private String removeLeadingZeros(String value) {
    if(!value.startsWith("0"))
      return value;

    int indexOfLastLeadingZero = 0;
    for (; indexOfLastLeadingZero + 1 < value.length(); indexOfLastLeadingZero++){
      char next = value.charAt(indexOfLastLeadingZero);

      if(next == '.'){// '0.' should be valid
        indexOfLastLeadingZero = indexOfLastLeadingZero - 1;
        break;
      } if(next != '0'){
        break;
      }
    }

    return value.substring(indexOfLastLeadingZero);
  }

}
