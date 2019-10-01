/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2;

import org.raml.v2.api.model.v10.system.types.AnnotableSimpleType;

public class ParserV2Utils {

  public static String nullSafe(AnnotableSimpleType<?> simpleType) {
    return simpleType != null ? String.valueOf(simpleType.value()) : null;
  }

}
