/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.parameter.Parameter;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class ResponseImpl implements Response {

  private final amf.client.model.domain.Response response;
  private Map<String, MimeType> body;

  public ResponseImpl(amf.client.model.domain.Response response) {
    this.response = response;
  }

  @Override
  public Map<String, MimeType> getBody() {
    if (body == null) {
      body = loadBody(response);
    }
    return body;
  }

  private static Map<String, MimeType> loadBody(amf.client.model.domain.Response response) {
    return response.payloads().stream()
        .filter(p -> p.mediaType().nonNull())
        .collect(toMap(p -> p.mediaType().value(), MimeTypeImpl::new));
  }

  @Override
  public boolean hasBody() {
    return !getBody().isEmpty();
  }

  @Override
  public Map<String, Parameter> getHeaders() {
    return null;
  }

  @Override
  public void setBody(Map<String, MimeType> body) {

  }

  @Override
  public void setHeaders(Map<String, Parameter> headers) {

  }

  @Override
  public Object getInstance() {
    return null;
  }

  @Override
  public Map<String, String> getExamples() {
    final Map<String, String> result = Response.super.getExamples();

    response.examples().forEach(example -> result.put(example.mediaType().value(), example.value().value()));

    return result;
  }
}
