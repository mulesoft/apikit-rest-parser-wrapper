/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.model;

import org.mule.apikit.implv1.model.parameter.ParameterImpl;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.apikit.validation.ApiValidationResult;
import org.raml.model.parameter.FormParameter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MimeTypeImpl implements MimeType {

  org.raml.model.MimeType mimeType;

  public MimeTypeImpl(org.raml.model.MimeType mimeType) {
    this.mimeType = mimeType;
  }

  public Object getCompiledSchema() {
    return mimeType.getCompiledSchema();
  }

  public String getSchema() {
    return mimeType.getSchema();
  }

  public Map<String, List<Parameter>> getFormParameters() {
    if (mimeType.getFormParameters() == null) {
      return null;
    }
    Map<String, List<Parameter>> map = new LinkedHashMap<>();
    for (Map.Entry<String, List<FormParameter>> entry : mimeType.getFormParameters().entrySet()) {
      List<Parameter> list = new ArrayList<>();
      entry.getValue().stream().forEach(formParameter -> list.add(new ParameterImpl(formParameter)));
      map.put(entry.getKey(), list);
    }
    return map;
  }

  public String getType() {
    return mimeType.getType();
  }

  public String getExample() {
    return mimeType.getExample();
  }

  public org.raml.model.MimeType getInstance() {
    return mimeType;
  }

  @Override
  public List<ApiValidationResult> validate(String payload) {
    throw new UnsupportedOperationException();
  }
}
