/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.validation;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.ArrayShape;
import amf.client.model.domain.NodeShape;
import amf.client.model.domain.Parameter;
import amf.client.model.domain.ScalarShape;
import amf.client.model.domain.Shape;
import amf.client.model.domain.UnionShape;
import org.apache.commons.collections.CollectionUtils;
import org.mule.amf.impl.util.AMFUtils;

import java.util.Optional;

import static org.mule.amf.impl.util.AMFUtils.castToAnyShape;
import static org.mule.amf.impl.util.AMFUtils.getSchemaFromContent;

public class ParameterValidationStrategyFactory {

  private ParameterValidationStrategyFactory() {
    throw new IllegalStateException("Utility class");
  }

  public static ParameterValidationStrategy getStrategy(Parameter parameter) {
    Shape shape = parameter.schema();
    if (shape == null) {
      return new JsonParameterValidationStrategy(castToAnyShape(getSchemaFromContent(parameter)));
    }
    AnyShape anyShape = castToAnyShape(shape);

    if (isOASDefaultSerialization(parameter)) {
      if (!parameter.explode().value()) {
        if (anyShape instanceof NodeShape) {
          return new OASObjectValidationStrategy((NodeShape) anyShape);
        }
        if (anyShape instanceof ArrayShape) {
          return new OASArrayValidationStrategy((ArrayShape) anyShape);
        }
        if (anyShape instanceof ScalarShape) {
          return new JsonParameterValidationStrategy(anyShape, AMFUtils.needsQuotes(anyShape));
        }
      }
    }

    return getStrategy(anyShape);
  }

  public static ParameterValidationStrategy getStrategy(AnyShape anyShape) {
    Boolean schemaNeedsQuotes = AMFUtils.needsQuotes(anyShape);

    if (isYamlValidationNeeded(anyShape)) {
      return new YamlParameterValidationStrategy(anyShape, schemaNeedsQuotes);
    }

    return getJsonParameterValidationStrategy(anyShape, schemaNeedsQuotes);
  }


  private static boolean isOASDefaultSerialization(Parameter parameter) {
    Optional<String> styleOption = parameter.style().option();
    Optional<Object> explodeOption = parameter.explode().option();

    if (styleOption.isPresent() && explodeOption.isPresent()) {// Style and explode are only present when vendor is OAS
      String style = styleOption.get();
      Boolean explode = (Boolean) explodeOption.get();

      return ("simple".equals(style) && !explode) // Defaults for uri param and query param
          || ("form".equals(style) && explode); // Default for headers
    }

    return false;
  }


  private static JsonParameterValidationStrategy getJsonParameterValidationStrategy(AnyShape anyShape,
                                                                                    boolean schemaNeedsQuotes) {
    return new JsonParameterValidationStrategy(anyShape, schemaNeedsQuotes);
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
