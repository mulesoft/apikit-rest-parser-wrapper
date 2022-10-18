/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.util;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.Parameter;
import amf.client.model.domain.Shape;
import org.mule.amf.impl.exceptions.UnsupportedSchemaException;

public class AMFUtils {

  final static String APPLICATION_JSON = "application/json";

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
}
