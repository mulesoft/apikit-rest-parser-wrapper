/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.execution.ExecutionEnvironment;
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

  private final LazyValue<PayloadValidator> jsonValidator ;
  private final LazyValue<ValidationReport> nullValidationReport ;

  JsonParameterValidationStrategy(AnyShape anyShape, boolean needsQuotes, ExecutionEnvironment executionEnvironment){
    this.needsQuotes = needsQuotes;

    this.nullValidationReport = new LazyValue<>(() -> {
      final PayloadValidator yamlPayloadValidator = anyShape.payloadValidator(APPLICATION_YAML, executionEnvironment)
              .orElseThrow(() -> new ParserException(APPLICATION_YAML + " validator not found for shape " + anyShape));

      return yamlPayloadValidator.syncValidate(APPLICATION_YAML, "null");
    });

    this.jsonValidator = new LazyValue<>(() -> anyShape.payloadValidator(APPLICATION_JSON, executionEnvironment)
            .orElseThrow(() -> new ParserException(APPLICATION_JSON + " validator not found for shape " + anyShape)));

  }

  public ValidationReport validate(String value) {
    if(value == null){
      return nullValidationReport.get();
    }

    return jsonValidator.get().syncValidate(APPLICATION_JSON,  needsQuotes ?
            quote(value.replaceAll("\"", "\\\\\"")) : value);
  }

}
