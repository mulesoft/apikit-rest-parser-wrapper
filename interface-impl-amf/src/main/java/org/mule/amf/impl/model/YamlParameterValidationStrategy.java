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

import static org.mule.amf.impl.model.MediaType.APPLICATION_YAML;

class YamlParameterValidationStrategy implements ParameterValidationStrategy {

  private AnyShape anyShape;

  private final LazyValue<PayloadValidator> parameterValidator =
      new LazyValue<>(() -> anyShape.parameterValidator(APPLICATION_YAML)
          .orElseThrow(() -> new ParserException(APPLICATION_YAML + " validator not found for shape " + anyShape)));

  YamlParameterValidationStrategy(AnyShape anyShape) {
    this.anyShape = anyShape;
  }

  @Override
  public ValidationReport validate(String value) {
    String payload = value != null ? value : "null";

    return parameterValidator.get().syncValidate(APPLICATION_YAML, payload);
  }
}
