/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import static java.util.stream.Collectors.toMap;

import amf.client.execution.ExecutionEnvironment;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.parameter.Parameter;

import java.util.Map;

public class ResponseImpl implements Response {

  private final ExecutionEnvironment executionEnvironment;
  amf.client.model.domain.Response response;

  public ResponseImpl(amf.client.model.domain.Response response, ExecutionEnvironment executionEnvironment) {
    this.response = response;
    this.executionEnvironment = executionEnvironment;
  }

  @Override
  public Map<String, MimeType> getBody() {
    return response.payloads().stream()
        .filter(p -> p.mediaType().nonNull())
        .collect(toMap(p -> p.mediaType().value(), payload -> new MimeTypeImpl(payload, executionEnvironment)));
  }

  @Override
  public boolean hasBody() {
    return !response.payloads().isEmpty() && response.payloads().stream().anyMatch(p -> p.mediaType().nonNull());
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
