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

import java.util.function.Supplier;

import static org.mule.amf.impl.model.MediaType.APPLICATION_JSON;
import static org.mule.amf.impl.model.MediaType.APPLICATION_YAML;
import static org.mule.amf.impl.model.ParameterImpl.quote;

class JsonParameterValidationStrategy implements ParameterValidationStrategy {
  private final boolean needsQuotes;
  private AnyShape anyShape;

  private final Supplier<PayloadValidator> jsonValidator = () -> anyShape.payloadValidator(APPLICATION_JSON)
          .orElseThrow(() -> new ParserException(APPLICATION_JSON + " validator not found for shape " + anyShape));

  private final Supplier<ValidationReport> nullValidationReport = () ->{
    final PayloadValidator yamlPayloadValidator = anyShape.payloadValidator(APPLICATION_YAML)
            .orElseThrow(() -> new ParserException(APPLICATION_YAML + " validator not found for shape " + anyShape));

    return yamlPayloadValidator.syncValidate(APPLICATION_YAML, "null");
  };

  JsonParameterValidationStrategy(AnyShape anyShape, boolean needsQuotes){
    this.anyShape = anyShape;
    this.needsQuotes = needsQuotes;
  }

  public ValidationReport validate(String value) {
    if(value == null){
      return nullValidationReport.get();
    }

    return jsonValidator.get().syncValidate(APPLICATION_JSON,  needsQuotes ?
            quote(value.replaceAll("\"", "\\\\\"")) : value);
  }
}
