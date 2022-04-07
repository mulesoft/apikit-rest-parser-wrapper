/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.model;

import org.mule.apikit.model.parameter.Parameter;

import java.util.List;
import java.util.Map;

public interface QueryString {

  String getDefaultValue();

  boolean isArray();

  /**
   * The method receives a YAML representation of the query string and return true if validates against the schema, false
   * otherwise.
   *
   * @deprecated This method is no longer acceptable to validate query strings. Use {@link QueryString#validate(Map)} instead.
   *
   * @param value a YAML representation of the query string.
   * @return true if validates against the schema false otherwise.
   */
  boolean validate(String value);

  boolean isScalar();

  boolean isFacetArray(String facet);

  Map<String, Parameter> facets();

  /**
   * The method receives a map containing all the query parameters and return true if validates against the schema, false
   * otherwise.
   * 
   * @since 2.2.7
   * @param queryParamsMap a map containing all the query parameters.
   * @return true if validates against the schema, false otherwise.
   */
  boolean validate(Map<String, List<String>> queryParamsMap);
}
