/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.common;

import org.mule.apikit.model.parameter.Parameter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.valueOf;

public class ValidationUtils {

  public static boolean validateQueryParams(Map<String, List<String>> queryParams, Map<String, Parameter> facets,
                                            List<QueryStringGroup> queryStringGroups) {
    Set<String> validatedQueryParamsKeys = new HashSet<>();
    for (Map.Entry<String, List<String>> queryParam : queryParams.entrySet()) {
      String value;
      Parameter expected = facets.get(queryParam.getKey());
      if (expected != null) {
        List<String> values = queryParam.getValue();
        if (expected.isArray()) {
          value = toArrayYamlRepresentation(queryParam, expected);
        } else {
          if (values.size() > 1) {
            return false;
          }
          value = values.isEmpty() ? null : values.get(0);
        }

        if (!expected.validate(value)) {
          return false;
        }
      }
      validatedQueryParamsKeys.add(queryParam.getKey());
    }
    for (QueryStringGroup group : queryStringGroups) {
      if (validatedQueryParamsKeys.containsAll(group.getRequiredProperties())
          && (group.supportAdditionalProperties() || !group.hasAdditionalProperties(validatedQueryParamsKeys))) {
        return true;
      }
    }

    return false;
  }

  private static String toArrayYamlRepresentation(Map.Entry<String, List<String>> queryParam, Parameter expected) {

    StringBuilder builder = new StringBuilder();
    queryParam.getValue().forEach(paramValue -> {
      String value = valueOf(paramValue);
      builder.append("- ");
      builder.append(escapeAndSurroundWithQuotesIfNeeded(expected, value));
      builder.append("\n");
    });

    return builder.toString();
  }

  private static String escapeAndSurroundWithQuotesIfNeeded(Parameter facet, String value) {
    return facet != null && value != null
        && (facet.isScalar() || (facet.isArray() && (!value.startsWith("{") && !value.startsWith("-"))))
            ? facet.surroundWithQuotesIfNeeded(value.replace("\"", "\\\""))
            : value;
  }

  public static class QueryStringGroup {

    private Set<String> requiredProperties;
    private Set<String> properties;
    private Boolean supportAdditionalProperties;

    private QueryStringGroup() {}

    private QueryStringGroup(Set<String> requiredProperties, Boolean additionalPropertiesProperties, Set<String> properties) {
      this.requiredProperties = requiredProperties;
      this.properties = properties;
      this.supportAdditionalProperties = additionalPropertiesProperties;
    }

    public Set<String> getRequiredProperties() {
      return requiredProperties;
    }

    boolean hasAdditionalProperties(Set<String> inProperties) {
      return inProperties.stream().anyMatch(property -> !properties.contains(property));
    }

    public Boolean supportAdditionalProperties() {
      return supportAdditionalProperties;
    }

    public static QueryStringGroupBuilder builder() {
      return new QueryStringGroupBuilder();
    }

    public static class QueryStringGroupBuilder {

      private Set<String> requiredProperties = new HashSet<>();
      private Set<String> properties = new HashSet<>();
      private boolean supportAdditionalProperties = true;

      QueryStringGroupBuilder() {}

      public QueryStringGroupBuilder supportAdditionalProperties(boolean supportAdditionalProperties) {
        this.supportAdditionalProperties = supportAdditionalProperties;
        return this;
      }

      private QueryStringGroupBuilder withRequiredProperty(String property) {
        requiredProperties.add(property);
        return withProperty(property);
      }

      private QueryStringGroupBuilder withProperty(String property) {
        properties.add(property);
        return this;
      }

      public QueryStringGroupBuilder withProperty(String property, boolean isRequired) {
        return isRequired ? this.withRequiredProperty(property) : this.withProperty(property);
      }

      public QueryStringGroup build() {
        return new QueryStringGroup(requiredProperties, supportAdditionalProperties, properties);
      }
    }
  }
}
