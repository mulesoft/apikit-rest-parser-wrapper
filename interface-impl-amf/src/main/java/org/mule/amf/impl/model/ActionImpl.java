/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.Operation;
import amf.client.model.domain.Payload;
import amf.client.model.domain.Request;
import amf.client.model.domain.Shape;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.ActionType;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.QueryString;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.SecurityReference;
import org.mule.apikit.model.parameter.Parameter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ActionImpl implements Action {

  private static final String VERSION = "version";
  private static final Predicate<amf.client.model.domain.Parameter> IS_NOT_VERSION =
      p -> !VERSION.equals(p.parameterName().value());
  private static final String APPLICATION_JSON = "application/json";
  private static final String APPLICATION_XML = "application/xml";

  private final ResourceImpl resource;
  private final Operation operation;
  private Map<String, MimeType> bodies;
  private Map<String, Response> responses;
  private Map<String, Parameter> queryParameters;
  private Map<String, Parameter> headers;
  private Map<String, Parameter> resolvedUriParameters;
  private QueryString queryString;
  private String successStatusCode;

  public ActionImpl(final ResourceImpl resource, final Operation operation) {
    this.resource = resource;
    this.operation = operation;
    this.queryString = initializeQueryString(operation);
  }

  @Override
  public ActionType getType() {
    return ActionType.valueOf(operation.method().value().toUpperCase());
  }

  @Override
  public boolean hasBody() {
    return !getBody().isEmpty();
  }

  @Override
  public Map<String, Response> getResponses() {
    if (responses == null) {
      responses = loadResponses(operation);
    }
    return responses;
  }

  private static Map<String, Response> loadResponses(final Operation operation) {
    Map<String, Response> result = new LinkedHashMap<>();
    for (amf.client.model.domain.Response response : operation.responses()) {
      result.put(response.statusCode().value(), new ResponseImpl(response));
    }
    return result;
  }

  @Override
  public Resource getResource() {
    return resource;
  }

  @Override
  public Map<String, MimeType> getBody() {
    if (bodies == null) {
      bodies = loadBodies(operation);
    }

    return bodies;
  }

  private static Map<String, MimeType> loadBodies(final Operation operation) {
    final Request request = operation.request();
    if (request == null) {
      return emptyMap();
    }

    final Map<String, MimeType> result = new LinkedHashMap<>();

    request.payloads().stream()
        .filter(payload -> payload.schema() != null)
        .forEach(payload -> addMimeTypes(result, payload));

    return result;
  }

  private static void addMimeTypes(Map<String, MimeType> result, Payload payload) {
    if (payload.mediaType().nonNull()) {
      result.put(payload.mediaType().value(), new MimeTypeImpl(payload));
    } else {
      result.put(APPLICATION_JSON, new MimeTypeImpl(payload));
      result.put(APPLICATION_XML, new MimeTypeImpl(payload));
    }
  }

  @Override
  public Map<String, Parameter> getQueryParameters() {
    if (queryParameters == null) {
      queryParameters = loadQueryParameters(operation);
    }
    return queryParameters;
  }

  private static Map<String, Parameter> loadQueryParameters(final Operation operation) {
    final Request request = operation.request();
    if (request == null) {
      return emptyMap();
    }

    final Map<String, Parameter> result = new HashMap<>();
    request.queryParameters().forEach(parameter -> {
      result.put(parameter.parameterName().value(), new ParameterImpl(parameter));
    });
    return result;
  }

  @Override
  public Map<String, List<Parameter>> getBaseUriParameters() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, Parameter> getResolvedUriParameters() {
    if (resolvedUriParameters == null) {
      resolvedUriParameters = loadResolvedUriParameters(resource, operation);
    }

    return resolvedUriParameters;
  }

  /**
   * Looks for all the uri parameters found either from the resource or from the operation's request (if any).
   * "Version" is an special uri param so it is ignored.
   *
   * @param resource
   * @return
   */
  private static Map<String, Parameter> loadResolvedUriParameters(final Resource resource, Operation operation) {
    Map<String, Parameter> operationUriParams = new HashMap<>();
    if (operation.request() != null) {
      List<amf.client.model.domain.Parameter> collectedUriParams = operation.request().uriParameters().stream()
          .filter(IS_NOT_VERSION).collect(toList());
      // If key is duplicated it means that it is declared at resource level and it is overridden in method, so keep the last one
      operationUriParams =
          collectedUriParams.stream().collect(toMap(p -> p.parameterName().value(), ParameterImpl::new, (p1, p2) -> p2));
    }
    final Map<String, Parameter> uriParameters = resource.getResolvedUriParameters();
    uriParameters.forEach(operationUriParams::putIfAbsent);

    return operationUriParams;
  }

  @Override
  public Map<String, Parameter> getHeaders() {
    if (headers == null) {
      headers = loadHeaders(operation);
    }
    return headers;
  }

  private Map<String, Parameter> loadHeaders(final Operation operation) {
    final Request request = operation.request();
    if (request == null) {
      return emptyMap();
    }

    final Map<String, Parameter> result = new HashMap<>();
    request.headers().forEach(parameter -> {
      result.put(parameter.parameterName().value(), new ParameterImpl(parameter));
    });
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
    return queryString;
  }

  @Override
  public String getSuccessStatusCode() {
    if (successStatusCode == null) {
      successStatusCode = Action.super.getSuccessStatusCode();
    }
    return successStatusCode;
  }

  private QueryString initializeQueryString(Operation op) {
    Request request = op.request();
    Shape shape = request != null ? request.queryString() : null;
    return shape != null ? new QueryStringImpl((AnyShape) shape) : null;
  }

}
