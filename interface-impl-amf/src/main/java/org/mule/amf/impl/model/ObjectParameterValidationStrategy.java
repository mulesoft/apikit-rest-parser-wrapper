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
import static org.mule.amf.impl.model.MediaType.APPLICATION_YAML;

public class ObjectParameterValidationStrategy implements ParameterValidationStrategy {


  private final PayloadValidator parameterValidator;

  ObjectParameterValidationStrategy(AnyShape anyShape){
    this.parameterValidator = anyShape.parameterValidator(APPLICATION_YAML).get();
  }

  @Override
  public ValidationReport validate(String value) {
    String payload = value != null ? value : "null" ;

    return parameterValidator.syncValidate(APPLICATION_YAML,payload);
  }
}
