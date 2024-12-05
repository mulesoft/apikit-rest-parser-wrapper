/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.apicontract.client.platform.AMFElementClient;
import amf.shapes.client.platform.model.domain.AnyShape;
import amf.shapes.client.platform.model.domain.ArrayShape;
import amf.shapes.client.platform.model.domain.UnionShape;
import org.apache.commons.collections4.CollectionUtils;

class ParameterValidationStrategyFactory {

  private ParameterValidationStrategyFactory() {
    throw new IllegalStateException("Utility class");
  }

  static ParameterValidationStrategy getStrategy(AnyShape anyShape, boolean needsCharEscaping,
                                                 AMFElementClient amfElementClient) {

    return isYamlValidationNeeded(anyShape) ? new YamlParameterValidationStrategy(anyShape, needsCharEscaping, amfElementClient)
        : new JsonParameterValidationStrategy(amfElementClient, anyShape, needsCharEscaping);
  }

  /**
   * Need to validate as YAML in case of - ArrayShape - UnionShape - AnyShape containing an aggregation of shapes for OAS 3.0
   * inheritance and polymorphism (oneOf, anyOf, allOf or not)
   *
   * @param anyShape
   * @return whether YAML validation is needed
   */
  private static boolean isYamlValidationNeeded(AnyShape anyShape) {
    return anyShape instanceof ArrayShape || anyShape instanceof UnionShape
        || CollectionUtils.isNotEmpty(anyShape.or()) || CollectionUtils.isNotEmpty(anyShape.and())
        || CollectionUtils.isNotEmpty(anyShape.xone()) || anyShape.not() != null;
  }
}
