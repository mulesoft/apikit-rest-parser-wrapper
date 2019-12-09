/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import static java.util.stream.Collectors.toMap;
import static org.mule.apikit.ParserUtils.resolveVersion;

import amf.client.execution.ExecutionEnvironment;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.ActionType;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.parameter.Parameter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import amf.client.model.domain.EndPoint;

public class ResourceImpl implements Resource {

  private final ExecutionEnvironment executionEnvironment;
  private AMFImpl amf;
  private EndPoint endPoint;
  private Map<ActionType, Action> actions;
  private Map<String, Parameter> resolvedUriParameters;

  ResourceImpl(final AMFImpl amf, final EndPoint endPoint, ExecutionEnvironment executionEnvironment) {
    this.amf = amf;
    this.endPoint = endPoint;
    this.executionEnvironment = executionEnvironment;
  }

  @Override
  public String getRelativeUri() {
    return endPoint.relativePath();
  }

  @Override
  public String getUri() {
    return endPoint.path().value();
  }

  @Override
  public String getResolvedUri(final String version) {
    return resolveVersion(getUri(), version);
  }

  @Override
  public String getParentUri() {
    return getUri().substring(0, getUri().length() - getRelativeUri().length());
  }

  @Override
  public Action getAction(final String name) {
    return getActions().get(getActionKey(name));
  }

  @Override
  public Map<ActionType, Action> getActions() {
    if (actions == null)
      actions = loadActions(endPoint, executionEnvironment);

    return actions;
  }

  private Map<ActionType, Action> loadActions(final EndPoint endPoint, ExecutionEnvironment executionEnvironment) {
    final Map<ActionType, Action> map = new LinkedHashMap<>();
    endPoint.operations()
        .forEach(operation -> map.put(getActionKey(operation.method().value()), new ActionImpl(this, operation)));
    return map;
  }

  private static ActionType getActionKey(final String method) {
    return ActionType.valueOf(method.toUpperCase());
  }

  @Override
  public Map<String, Resource> getResources() {
    return amf.getResources(this);
  }

  @Override
  public String getDisplayName() {
    final String value = endPoint.name().value();
    return value != null ? value : getRelativeUri();
  }

  @Override
  public Map<String, Parameter> getResolvedUriParameters() {
    if (resolvedUriParameters == null) {
      resolvedUriParameters = loadResolvedUriParameters(endPoint, executionEnvironment);
    }

    return resolvedUriParameters;
  }

  private static Map<String, Parameter> loadResolvedUriParameters(final EndPoint resource, ExecutionEnvironment executionEnvironment) {
    return resource.parameters().stream()
        .filter(p -> !"version".equals(p.name().value())) // version is an special uri param so it is ignored
        .collect(toMap(p -> p.name().value(),p -> new ParameterImpl(p,executionEnvironment)));
  }

  @Override
  public void setParentUri(String parentUri) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, List<Parameter>> getBaseUriParameters() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void cleanBaseUriParameters() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return getUri();
  }

  public ExecutionEnvironment getExecutionEnvironment() {
    return executionEnvironment;
  }
}
