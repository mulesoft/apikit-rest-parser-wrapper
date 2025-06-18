/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.attributes;

import org.mule.validation.helpers.AttributesHelper;
import java.util.List;
import org.mule.apikit.model.Action;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.runtime.api.util.MultiMap;

public class HeadersDefaultsHandler implements DefaultsHandler {

  private final Action action;

  public HeadersDefaultsHandler(Action action) {
    this.action = action;
  }

  @Override
  public HttpRequestAttributes addDefaultValues(HttpRequestAttributes attributes) {
    MultiMap<String, String> defaultValues = new MultiMap<>();
    MultiMap<String, String> params = attributes.getHeaders();

    action.getHeaders().entrySet().stream().forEach(entry -> {
      List<String> values = AttributesHelper.getParamsIgnoreCase(params, entry.getKey());
      if (values.isEmpty() && entry.getValue().getDefaultValue() != null) {
        defaultValues.put(entry.getKey(), entry.getValue().getDefaultValue());
      }
    });

    if (defaultValues.size() != 0) {
      addInputQueryParams(params, defaultValues);
      return new HttpRequestAttributesBuilder(attributes)
          .headers(defaultValues)
          .build();
    }
    return attributes;
  }

  private void addInputQueryParams(MultiMap<String, String> inputQueryParams, MultiMap<String, String> defaults) {
    inputQueryParams.entrySet().stream().forEach(entry -> {
      defaults.put(entry.getKey(), entry.getValue());
    });
  }
}
