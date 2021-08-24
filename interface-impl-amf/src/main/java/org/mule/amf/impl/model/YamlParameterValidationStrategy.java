/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.apicontract.client.platform.APIConfiguration;
import amf.core.client.common.validation.ValidationMode;
import amf.core.client.platform.validation.AMFValidationReport;
import amf.core.client.platform.validation.payload.AMFShapePayloadValidator;
import amf.shapes.client.platform.model.domain.AnyShape;
import org.mule.amf.impl.util.LazyValue;

import static org.mule.amf.impl.model.MediaType.APPLICATION_YAML;

class YamlParameterValidationStrategy implements ParameterValidationStrategy {

  private AnyShape anyShape;

  private final LazyValue<AMFShapePayloadValidator> parameterValidator =
      new LazyValue<>(() -> APIConfiguration.API().elementClient().payloadValidatorFor(anyShape, APPLICATION_YAML,
                                                                                       ValidationMode
                                                                                           .ScalarRelaxedValidationMode()));

  YamlParameterValidationStrategy(AnyShape anyShape) {
    this.anyShape = anyShape;
  }

  @Override
  public AMFValidationReport validate(String value) {
    String payload = value != null ? value : "null";

    return parameterValidator.get().syncValidate(payload);
  }
}
