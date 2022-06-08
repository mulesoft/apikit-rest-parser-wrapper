/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit;

import org.mule.apikit.model.parameter.Parameter;

import java.util.Collection;
import java.util.Map;

import static java.lang.String.valueOf;

public class ParserUtils {

  public static String resolveVersion(String path, String version) {
    if (path == null) {
      throw new IllegalArgumentException("path cannot be null");
    }
    if (!path.contains("{version}")) {
      return path;
    }
    if (version == null) {
      throw new IllegalStateException("RAML does not contain version information and is required by resource: " + path);
    }
    return path.replaceAll("\\{version}", version);
  }

  /**
   * Builds a YAML for provided array of values.
   *
   * @param facet
   * @param paramValues
   * @return YAML representation of array values.
   */
  public static String getArrayAsYamlValue(Parameter facet, Collection<?> paramValues) {
    if (paramValues == null) {
      return null;
    }
    StringBuilder builder = new StringBuilder();

    paramValues.forEach(paramValue -> {
      String value = valueOf(paramValue);
      builder.append("- ");
      builder.append(facet.surroundWithQuotesIfNeeded(value));
      builder.append("\n");
    });

    return builder.toString();
  }

  /**
   * Builds a YAML from provided Query String.
   *
   * @param facets
   * @param queryParamsCopy
   * @return Query String as YAML
   */
  public static String queryStringAsYamlValue(Map<String, Parameter> facets,
                                              Map<String, Collection<?>> queryParamsCopy) {
    StringBuilder queryStringYaml = new StringBuilder();
    Parameter facet;
    for (Object property : queryParamsCopy.keySet()) {
      facet = facets.get(property.toString());
      if (facet == null) {
        return "";
      }
      final Collection<?> actualQueryParam = queryParamsCopy.get(property.toString());

      queryStringYaml.append("\n").append(property).append(": ");

      if (actualQueryParam == null) {
        queryStringYaml.append(facet.surroundWithQuotesIfNeeded(null)).append("\n");
      } else if (actualQueryParam.size() > 1 || facet.isArray()) {
        for (Object value : actualQueryParam) {
          queryStringYaml.append("\n  - ").append(facet.surroundWithQuotesIfNeeded(valueOf(value)));
        }
        queryStringYaml.append("\n");
      } else {
        for (Object value : actualQueryParam) {
          queryStringYaml.append(facet.surroundWithQuotesIfNeeded(valueOf(value))).append("\n");
        }
      }
    }
    return queryStringYaml.toString();
  }

  /**
   * Surrounds payload value with quotes.
   *
   * @param payload
   * @return Quoted payload
   */
  public static String quoteValue(String payload) {
    return "\"" + payload + "\"";
  }

  /**
   * Escapes back slashes and quotes from value.
   *
   * @param value
   * @return escaped value
   */
  public static String escapeSpecialCharsInYamlValue(String value) {
    return value == null ? null : value.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
