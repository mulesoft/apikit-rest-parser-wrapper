/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.util;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.ArrayShape;
import amf.client.model.domain.Parameter;
import amf.client.model.domain.ScalarShape;
import amf.client.model.domain.Shape;
import amf.client.model.domain.UnionShape;
import com.google.common.collect.ImmutableSet;
import org.mule.amf.impl.exceptions.UnsupportedSchemaException;

import java.util.Set;

public class AMFUtils {

  public final static String APPLICATION_JSON = "application/json";
  public final static String APPLICATION_YAML = "application/yaml";
  public static final String BOOLEAN_DATA_TYPE = "boolean";
  public static final Set<String> NUMBER_DATA_TYPES = ImmutableSet.of("integer", "float", "number", "long", "double");

  public AMFUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static Shape getSchemaFromContent(Parameter parameter) {
    return (parameter.payloads().stream().filter(p -> APPLICATION_JSON.equals(p.mediaType().value())).findFirst()
        .orElseThrow(() -> new UnsupportedSchemaException())).schema();
  }

  public static AnyShape castToAnyShape(Shape shape) {
    if (shape instanceof AnyShape) {
      return (AnyShape) shape;
    }
    throw new UnsupportedSchemaException();
  }

  public static Boolean needsQuotes(Shape anyShape) {
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
}
