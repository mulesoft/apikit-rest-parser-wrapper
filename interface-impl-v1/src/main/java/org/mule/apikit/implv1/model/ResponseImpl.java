/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.mule.apikit.implv1.model.parameter.ParameterImpl;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.parameter.Parameter;

import org.raml.model.parameter.Header;

public class ResponseImpl implements Response {

  org.raml.model.Response response;

  public ResponseImpl(org.raml.model.Response response) {
    this.response = response;
  }

  public Map<String, MimeType> getBody() {
    if (response.getBody() == null) {
      return null;
    }
    Map<String, MimeType> map = new LinkedHashMap<String, MimeType>();
    for (Map.Entry<String, org.raml.model.MimeType> entry : response.getBody().entrySet()) {
      map.put(entry.getKey(), new MimeTypeImpl(entry.getValue()));
    }
    return map;
  }

  public boolean hasBody() {
    return response.hasBody();
  }

  public Map<String, Parameter> getHeaders() {
    final Map<String, Parameter> map = new LinkedHashMap<>();

    Optional.ofNullable(response.getHeaders())
        .ifPresent(headers -> headers.forEach((name, header) -> map.put(name, new ParameterImpl(header))));

    return map;
  }

  public void setBody(Map<String, MimeType> body) {
    Map<String, org.raml.model.MimeType> map = new LinkedHashMap<String, org.raml.model.MimeType>();
    for (Map.Entry<String, MimeType> entry : body.entrySet()) {
      map.put(entry.getKey(), (org.raml.model.MimeType) entry.getValue().getInstance());
    }
    response.setBody(map);
  }

  public void setHeaders(Map<String, Parameter> headers) {
    Map<String, Header> map = new LinkedHashMap<String, Header>();
    for (Map.Entry<String, Parameter> entry : headers.entrySet()) {
      map.put(entry.getKey(), (Header) entry.getValue().getInstance());
    }
    response.setHeaders(map);
  }

  public Object getInstance() {
    return response;
  }
}
