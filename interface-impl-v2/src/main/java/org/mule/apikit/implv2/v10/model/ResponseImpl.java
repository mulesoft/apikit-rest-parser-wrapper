/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v10.model;

import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.parameter.Parameter;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

public class ResponseImpl implements Response {

  private org.raml.v2.api.model.v10.bodies.Response response;
  private Map<String, MimeType> body;

  public ResponseImpl(org.raml.v2.api.model.v10.bodies.Response response) {
    this.response = response;
  }

  @Override
  public boolean hasBody() {
    return !getBody().isEmpty();
  }

  @Override
  public Map<String, MimeType> getBody() {
    if (body == null) {
      body = loadBody(response);
    }

    return body;
  }

  private static Map<String, MimeType> loadBody(org.raml.v2.api.model.v10.bodies.Response response) {
    Map<String, MimeType> result = new LinkedHashMap<>();
    for (TypeDeclaration typeDeclaration : response.body()) {
      result.put(typeDeclaration.name(), new MimeTypeImpl(typeDeclaration));
    }
    return result;
  }

  @Override
  public Map<String, Parameter> getHeaders() {
    final Map<String, Parameter> result = new LinkedHashMap<>();

    ofNullable(response.headers())
        .ifPresent(headers -> headers.forEach(header -> result.put(header.name(), new ParameterImpl(header))));

    return result;
  }

  @Override
  public void setBody(Map<String, MimeType> body) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setHeaders(Map<String, Parameter> headers) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getInstance() {
    throw new UnsupportedOperationException();
  }
}
