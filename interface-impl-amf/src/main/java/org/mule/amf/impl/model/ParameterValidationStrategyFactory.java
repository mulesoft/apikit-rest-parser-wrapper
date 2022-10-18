/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.ArrayShape;
import amf.client.model.domain.NodeShape;
import amf.client.model.domain.Parameter;
import amf.client.model.domain.ScalarShape;
import amf.client.model.domain.Shape;
import amf.client.model.domain.UnionShape;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections.CollectionUtils;
import org.mule.amf.impl.exceptions.UnsupportedSchemaException;
import org.mule.amf.impl.util.AMFUtils;

import java.util.Optional;
import java.util.Set;

import static org.mule.amf.impl.util.AMFUtils.castToAnyShape;
import static org.mule.amf.impl.util.AMFUtils.getSchemaFromContent;

public class ParameterValidationStrategyFactory {

  private ParameterValidationStrategyFactory() {
    throw new IllegalStateException("Utility class");
  }

  private static final Set<String> NUMBER_DATA_TYPES = ImmutableSet.of("integer", "float", "number", "long", "double");
  public static final String BOOLEAN_DATA_TYPE = "boolean";

  public static ParameterValidationStrategy getStrategy(AnyShape anyShape) {
    Boolean schemaNeedsQuotes = needsQuotes(anyShape);

    if (isYamlValidationNeeded(anyShape)) {
      return new YamlParameterValidationStrategy(anyShape, schemaNeedsQuotes);
    }

    return getJsonParameterValidationStrategy(anyShape, schemaNeedsQuotes);
  }

  public static ParameterValidationStrategy getStrategy(Parameter parameter) {
    Shape shape = parameter.schema();
    Boolean isJson = false;
    if (shape == null) {
      shape = getSchemaFromContent(parameter);
      isJson = true;
    }
    AnyShape anyShape = castToAnyShape(shape);
    Optional<String> style = parameter.style().option();

    if (style.isPresent()) { // is OAS
      if (isJson || (anyShape instanceof ScalarShape)) {
        return new JsonParameterValidationStrategy(anyShape, needsQuotes(anyShape));
      }
      if ("simple".equals(style.get())) {
        if (!parameter.explode().value()) {
          if (anyShape instanceof NodeShape) {
            return new OASObjectValidationStrategy((NodeShape) anyShape);
          }
          if (anyShape instanceof ArrayShape) {
            return new OASArrayValidationStrategy((ArrayShape) anyShape);
          }
        }
      }

    }

    return getStrategy(anyShape);
  }


  private static Boolean needsQuotes(Shape anyShape) {
    ScalarShape scalarShape = null;
    if (anyShape instanceof ScalarShape) {
      scalarShape = ((ScalarShape) anyShape);
    } else if (anyShape instanceof ArrayShape) {
      Shape itemsShape = ((ArrayShape) anyShape).items();
      scalarShape = itemsShape instanceof ScalarShape ? ((ScalarShape) itemsShape) : null;
    } else if (anyShape instanceof UnionShape) {
      return ((UnionShape) anyShape).anyOf().stream().anyMatch(shape -> needsQuotes(shape));
    }
    if (scalarShape == null) {
      return Boolean.FALSE;
    }
    String dataType = scalarShape.dataType().value();
    if (dataType == null) {
      return Boolean.FALSE;
    }
    dataType = dataType.substring(dataType.lastIndexOf('#') + 1);
    return !(NUMBER_DATA_TYPES.contains(dataType) || BOOLEAN_DATA_TYPE.equals(dataType));
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
