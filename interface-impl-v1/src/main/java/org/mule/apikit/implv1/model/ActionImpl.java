/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.model;

import static java.util.Collections.emptyMap;
import static org.mule.apikit.implv1.model.ResourceImpl.loadResolvedUriParameters;

import org.mule.apikit.implv1.model.parameter.ParameterImpl;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.ActionType;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.QueryString;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.SecurityReference;
import org.mule.apikit.model.parameter.Parameter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.raml.model.parameter.Header;
import org.raml.model.parameter.QueryParameter;
import org.raml.model.parameter.UriParameter;

public class ActionImpl implements Action {

  private org.raml.model.Action action;
  private Map<String, Parameter> resolvedUriParameters;

  public ActionImpl(org.raml.model.Action action) {
    this.action = action;
  }

  public ActionType getType() {
    return ActionType.valueOf(action.getType().name());
  }

  public Resource getResource() {
    org.raml.model.Resource resource = action.getResource();
    if (resource == null) {
      return null;
    }
    return new ResourceImpl(resource);

  }

  public Map<String, MimeType> getBody() {
    if (action.getBody() == null) {
      return null;
    }
    Map<String, MimeType> map = new LinkedHashMap<String, MimeType>();
    for (Map.Entry<String, org.raml.model.MimeType> entry : action.getBody().entrySet()) {
      map.put(entry.getKey(), new MimeTypeImpl(entry.getValue()));
    }
    return map;
  }

  public Map<String, List<Parameter>> getBaseUriParameters() {
    if (action.getBaseUriParameters() == null) {
      return null;
    }
    Map<String, List<Parameter>> map = new LinkedHashMap<String, List<Parameter>>();
    for (Map.Entry<String, List<UriParameter>> entry : action.getBaseUriParameters().entrySet()) {
      List<Parameter> list = new ArrayList<Parameter>();
      for (UriParameter parameter : entry.getValue()) {
        list.add(new ParameterImpl(parameter));
      }
      map.put(entry.getKey(), list);
    }
    return map;
  }

  @Override
  public Map<String, Parameter> getResolvedUriParameters() {
    if (resolvedUriParameters == null) {
      resolvedUriParameters = loadResolvedUriParameters(action.getResource());
    }

    return resolvedUriParameters;
  }

  public Map<String, Parameter> getQueryParameters() {
    if (action.getQueryParameters() == null) {
      return emptyMap();
    }
    Map<String, Parameter> map = new LinkedHashMap<String, Parameter>();
    for (Map.Entry<String, QueryParameter> entry : action.getQueryParameters().entrySet()) {
      map.put(entry.getKey(), new ParameterImpl(entry.getValue()));
    }
    return map;
  }

  public boolean hasBody() {
    return action.hasBody();
  }

  public Map<String, Response> getResponses() {
    if (action.getResponses() == null) {
      return null;
    }
    Map<String, Response> map = new LinkedHashMap<String, Response>();
    for (Map.Entry<String, org.raml.model.Response> entry : action.getResponses().entrySet()) {
      map.put(entry.getKey(), new ResponseImpl(entry.getValue()));
    }
    return map;
  }


  @Override
  public QueryString queryString() {
    return null;
  }

  public Map<String, Parameter> getHeaders() {
    if (action.getHeaders() == null) {
      return emptyMap();
    }
    Map<String, Parameter> map = new LinkedHashMap<String, Parameter>();
    for (Map.Entry<String, Header> entry : action.getHeaders().entrySet()) {
      map.put(entry.getKey(), new ParameterImpl(entry.getValue()));
    }
    return map;
  }

  public List<SecurityReference> getSecuredBy() {
    if (action.getSecuredBy() == null) {
      return null;
    }
    List<SecurityReference> list = new ArrayList<SecurityReference>();
    for (org.raml.model.SecurityReference securityReference : action.getSecuredBy()) {
      list.add(new SecurityReferenceImpl(securityReference));
    }
    return list;
  }

  public List<String> getIs() {
    return action.getIs();
  }

  public void cleanBaseUriParameters() {
    action.getBaseUriParameters().clear();
  }

  public void setHeaders(Map<String, Parameter> headers) {
    Map<String, Header> map = new LinkedHashMap<String, Header>();
    for (Map.Entry<String, Parameter> entry : headers.entrySet()) {
      map.put(entry.getKey(), (Header) entry.getValue().getInstance());
    }
    action.setHeaders(map);
  }

  public void setQueryParameters(Map<String, Parameter> queryParameters) {
    Map<String, QueryParameter> map = new LinkedHashMap<String, QueryParameter>();
    for (Map.Entry<String, Parameter> entry : queryParameters.entrySet()) {
      map.put(entry.getKey(), (QueryParameter) entry.getValue().getInstance());
    }
    action.setQueryParameters(map);
  }

  public void setBody(Map<String, MimeType> body) {
    Map<String, org.raml.model.MimeType> map = new LinkedHashMap<String, org.raml.model.MimeType>();
    for (Map.Entry<String, MimeType> entry : body.entrySet()) {
      map.put(entry.getKey(), (org.raml.model.MimeType) entry.getValue().getInstance());
    }
    action.setBody(map);
  }

  public void addResponse(String s, Response response) {
    action.getResponses().put(s, (org.raml.model.Response) response.getInstance());
  }

  public void addSecurityReference(String securityReferenceName) {
    action.getSecuredBy().add(new org.raml.model.SecurityReference(securityReferenceName));
  }

  public void addIs(String s) {
    action.getIs().add(s);
  }

}
