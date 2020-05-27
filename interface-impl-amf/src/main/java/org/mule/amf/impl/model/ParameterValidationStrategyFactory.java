/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.ArrayShape;
import amf.client.model.domain.ScalarShape;
import amf.client.model.domain.UnionShape;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections.CollectionUtils;

import java.util.Set;

class ParameterValidationStrategyFactory {
  private static final Set<String> NOT_QUOTABLE_DATA_TYPES = ImmutableSet.of("integer", "float", "number", "boolean");

  private ParameterValidationStrategyFactory() {
    throw new IllegalStateException("Utility class");
  }

  static ParameterValidationStrategy getStrategy(AnyShape anyShape) {
    return isYamlValidationNeeded(anyShape) ? new YamlParameterValidationStrategy(anyShape)
            : new JsonParameterValidationStrategy(anyShape, needsQuotes(anyShape));
  }

  private static boolean isYamlValidationNeeded(AnyShape anyShape) {
    return anyShape instanceof ArrayShape || anyShape instanceof UnionShape
            || CollectionUtils.isNotEmpty(anyShape.or()) || CollectionUtils.isNotEmpty(anyShape.and())
            || CollectionUtils.isNotEmpty(anyShape.xone()) || anyShape.not() != null;
  }

  private static boolean needsQuotes(AnyShape anyShape) {
    if (!(anyShape instanceof ScalarShape)) {
      return false;
    }

    String dataType = ((ScalarShape) anyShape).dataType().value();
    dataType = dataType.substring(dataType.lastIndexOf('#') + 1);

    return !NOT_QUOTABLE_DATA_TYPES.contains(dataType);
  }
}
