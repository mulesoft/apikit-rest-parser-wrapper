/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.ScalarShape;
import amf.client.validate.PayloadValidator;
import amf.client.validate.ValidationReport;

import static org.mule.amf.impl.model.MediaType.APPLICATION_JSON;
import static org.mule.amf.impl.model.MediaType.APPLICATION_YAML;
import static org.mule.amf.impl.model.ParameterImpl.quote;

public class ScalarParameterValidationStrategy implements ParameterValidationStrategy {
  final PayloadValidator jsonValidator;
  final PayloadValidator yamlValidator;
  private boolean needsQuotes;

  ScalarParameterValidationStrategy(AnyShape anyShape){
    this.jsonValidator = anyShape.payloadValidator(APPLICATION_JSON).get();
    this.yamlValidator = anyShape.payloadValidator(APPLICATION_YAML).get();
    this.needsQuotes = needsQuotes(anyShape);
  }

  private static boolean needsQuotes(AnyShape anyShape) {
    if(!(anyShape instanceof ScalarShape)){
      return false;
    }

    String dataType = ((ScalarShape) anyShape).dataType().value();
    dataType = dataType.substring(dataType.lastIndexOf("#") + 1);

    if(!(dataType.equals("integer") || dataType.equals("number") || dataType.equals("boolean"))){
      return true;
    }

    return false;
  }

  @Override
  public ValidationReport validate(String value) {
    String payload = value != null ? value : "null" ;

    if(value == null){
      return yamlValidator.syncValidate(APPLICATION_YAML,payload);
    }

    if(needsQuotes){
      payload =  quote(payload);
    }

    return jsonValidator.syncValidate(APPLICATION_JSON, payload);

  }

}
