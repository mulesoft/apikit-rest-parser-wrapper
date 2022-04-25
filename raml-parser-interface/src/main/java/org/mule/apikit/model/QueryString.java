/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.model;

import org.mule.apikit.model.parameter.Parameter;

import java.util.Collection;
import java.util.Map;

public interface QueryString {

  String getDefaultValue();

  boolean isArray();

  /**
   * Validates query string.
   * <p>
   * Deprecated since 2.3.0. Use {@link QueryString#validate(Map)} instead.
   *
   * @param value query string as YAML value
   * @return whether is valid or not
   */
  @Deprecated
  boolean validate(String value);

  /**
   * Validates query string.
   *
   * @param queryParams collection of parameters that are part of the query string
   * @return whether query string is valid or not
   * @since 2.3.0
   */
  boolean validate(Map<String, Collection<?>> queryParams);

  boolean isScalar();

  boolean isFacetArray(String facet);

  Map<String, Parameter> facets();
}
