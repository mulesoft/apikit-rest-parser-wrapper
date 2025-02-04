/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.attributes;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map.Entry;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.runtime.api.util.MultiMap;

public class QueryParamsDefaultsHandler implements DefaultsHandler {

  private final Action action;

  public QueryParamsDefaultsHandler(Action action) {
    this.action = action;
  }

  @Override
  public HttpRequestAttributes addDefaultValues(HttpRequestAttributes attributes) {
    MultiMap<String, String> defaultValues = new MultiMap<>();
    MultiMap<String, String> params = attributes.getQueryParams();
    String queryString = attributes.getQueryString();

    for (Entry<String, Parameter> entry : action.getQueryParameters().entrySet()) {

      List<String> values = params.getAll(entry.getKey());

      if (values.isEmpty() && entry.getValue().getDefaultValue() != null) {
        defaultValues.put(entry.getKey(), entry.getValue().getDefaultValue());

        queryString = addQueryString(queryString, entry.getKey(),
            entry.getValue().getDefaultValue());
      }
    }

    if (defaultValues.size() != 0) {
      addInputQueryParams(params, defaultValues);
      return new HttpRequestAttributesBuilder(attributes)
          .queryParams(defaultValues)
          .queryString(queryString)
          .build();
    }

    return attributes;
  }

  private void addInputQueryParams(MultiMap<String, String> inputQueryParams, MultiMap<String, String> defaults) {
    inputQueryParams.entrySet().stream().forEach(entry -> {
      defaults.put(entry.getKey(), entry.getValue());
    });
  }

  // TODO : refactor this
  private String addQueryString(String oldQueryString, String key, String value) {
    String newParam = oldQueryString.length() != 0 ? "&" : "";
    try {
      newParam += URLEncoder.encode(key, "UTF-8");
      if (value != null) {

        newParam += "=" + URLEncoder.encode(value, "UTF-8");
      }
    } catch (UnsupportedEncodingException e) {
      //UTF-8 will never be unsupported
    }
    return oldQueryString + newParam;
  }
}
