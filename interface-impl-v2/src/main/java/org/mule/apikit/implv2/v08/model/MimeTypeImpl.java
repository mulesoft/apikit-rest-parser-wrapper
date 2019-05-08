/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v08.model;

import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.apikit.validation.ApiValidationResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.raml.v2.api.model.v08.bodies.BodyLike;

public class MimeTypeImpl implements MimeType {

  private BodyLike bodyLike;

  public MimeTypeImpl(BodyLike bodyLike) {
    this.bodyLike = bodyLike;
  }

  @Override
  public String getType() {
    return bodyLike.name();
  }

  @Override
  public String getExample() {
    return bodyLike.example() != null ? bodyLike.example().value() : null;
  }

  @Override
  public String getSchema() {
    return bodyLike.schema() != null ? bodyLike.schema().value() : null;
  }

  @Override
  public Map<String, List<Parameter>> getFormParameters() {
    Map<String, List<Parameter>> result = new LinkedHashMap<>();
    for (org.raml.v2.api.model.v08.parameters.Parameter parameter : bodyLike.formParameters()) {
      List<Parameter> list = new ArrayList<>();
      list.add(new ParameterImpl(parameter));
      result.put(parameter.name(), list);
    }
    return result;
  }

  @Override
  public Object getCompiledSchema() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getInstance() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<ApiValidationResult> validate(String payload) {
    throw new UnsupportedOperationException();
  }
}
