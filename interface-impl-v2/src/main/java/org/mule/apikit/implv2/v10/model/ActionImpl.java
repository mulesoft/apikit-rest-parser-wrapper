/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v10.model;

import org.mule.apikit.model.Action;
import org.mule.apikit.model.ActionType;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.QueryString;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.SecurityReference;
import org.mule.apikit.model.parameter.Parameter;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.methods.Method;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mule.apikit.implv2.v10.model.ResourceImpl.loadResolvedUriParameters;

public class ActionImpl implements Action {

  private Method method;
  private Map<String, MimeType> bodies;
  private Map<String, Response> responses;
  private Map<String, Parameter> queryParameters;
  private Map<String, Parameter> headers;
  private Map<String, Parameter> resolvedUriParameters;
  private String successStatusCode;

  public ActionImpl(Method method) {
    this.method = method;
  }

  @Override
  public ActionType getType() {
    return ActionType.valueOf(method.method().toUpperCase());
  }

  @Override
  public boolean hasBody() {
    return !getBody().isEmpty();
  }

  @Override
  public Map<String, Response> getResponses() {
    if (responses == null) {
      responses = loadResponses(method);
    }
    return responses;
  }

  private static Map<String, Response> loadResponses(Method method) {
    Map<String, Response> result = new LinkedHashMap<>();
    for (org.raml.v2.api.model.v10.bodies.Response response : method.responses()) {
      result.put(response.code().value(), new ResponseImpl(response));
    }
    return result;
  }

  @Override
  public Resource getResource() {
    return new ResourceImpl(method.resource());
  }

  @Override
  public Map<String, MimeType> getBody() {
    if (bodies == null) {
      bodies = loadBodies(method);
    }

    return bodies;
  }

  private static Map<String, MimeType> loadBodies(Method method) {
    Map<String, MimeType> result = new LinkedHashMap<>();
    for (TypeDeclaration typeDeclaration : method.body()) {
      result.put(typeDeclaration.name(), new MimeTypeImpl(typeDeclaration));
    }
    return result;
  }

  @Override
  public Map<String, Parameter> getQueryParameters() {
    if (queryParameters == null) {
      queryParameters = loadQueryParameters(method);
    }
    return queryParameters;
  }

  private static Map<String, Parameter> loadQueryParameters(Method method) {
    final Map<String, Parameter> result = new HashMap<>();
    for (TypeDeclaration typeDeclaration : method.queryParameters()) {
      result.put(typeDeclaration.name(), new ParameterImpl(typeDeclaration));
    }
    return result;
  }

  @Override
  public Map<String, List<Parameter>> getBaseUriParameters() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, Parameter> getResolvedUriParameters() {
    if (resolvedUriParameters == null) {
      resolvedUriParameters = loadResolvedUriParameters(method.resource());
    }

    return resolvedUriParameters;
  }

  @Override
  public Map<String, Parameter> getHeaders() {
    if (headers == null) {
      headers = loadHeaders(method);
    }
    return headers;
  }

  private Map<String, Parameter> loadHeaders(Method method) {
    Map<String, Parameter> result = new HashMap<>();
    for (TypeDeclaration typeDeclaration : method.headers()) {
      result.put(typeDeclaration.name(), new ParameterImpl(typeDeclaration));
    }
    return result;
  }

  @Override
  public List<SecurityReference> getSecuredBy() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> getIs() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void cleanBaseUriParameters() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setHeaders(Map<String, Parameter> headers) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setQueryParameters(Map<String, Parameter> queryParameters) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBody(Map<String, MimeType> body) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addResponse(String key, Response response) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addSecurityReference(String securityReferenceName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addIs(String is) {
    throw new UnsupportedOperationException();
  }

  @Override
  public QueryString queryString() {
    return method.queryString() == null ? null : new QueryStringImpl(method.queryString());
  }

  @Override
  public String getSuccessStatusCode() {
    if (successStatusCode == null) {
      successStatusCode = Action.super.getSuccessStatusCode();
    }
    return successStatusCode;
  }
}
