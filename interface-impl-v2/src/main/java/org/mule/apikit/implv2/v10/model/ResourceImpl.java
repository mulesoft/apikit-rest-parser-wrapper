/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v10.model;

import org.mule.apikit.model.Action;
import org.mule.apikit.model.ActionType;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.parameter.Parameter;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.methods.Method;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mule.apikit.ParserUtils.resolveVersion;

public class ResourceImpl implements Resource {

  private org.raml.v2.api.model.v10.resources.Resource resource;
  private Map<ActionType, Action> actions;
  private Map<String, Parameter> resolvedUriParameters;

  public ResourceImpl(org.raml.v2.api.model.v10.resources.Resource resource) {
    this.resource = resource;
  }

  @Override
  public String getRelativeUri() {
    return resource.relativeUri().value();
  }

  @Override
  public String getUri() {
    return resource.resourcePath();
  }

  @Override
  public String getResolvedUri(String version) {
    return resolveVersion(getUri(), version);
  }


  @Override
  public String getParentUri() {
    return getUri().substring(0, getUri().length() - getRelativeUri().length());
  }

  @Override
  public Action getAction(String name) {
    return getActions().get(getActionKey(name));
  }

  @Override
  public Map<ActionType, Action> getActions() {
    if (actions == null) {
      actions = loadActions(resource);
    }
    return actions;
  }

  private static Map<ActionType, Action> loadActions(org.raml.v2.api.model.v10.resources.Resource resource) {
    Map<ActionType, Action> map = new LinkedHashMap<>();
    for (Method method : resource.methods()) {
      map.put(getActionKey(method.method()), new ActionImpl(method));
    }
    return map;
  }

  private static ActionType getActionKey(String method) {
    return ActionType.valueOf(method.toUpperCase());
  }

  @Override
  public Map<String, Resource> getResources() {
    Map<String, Resource> result = new HashMap<>();
    for (org.raml.v2.api.model.v10.resources.Resource item : resource.resources()) {
      result.put(item.relativeUri().value(), new ResourceImpl(item));
    }
    return result;
  }

  @Override
  public String getDisplayName() {
    return resource.displayName() != null ? String.valueOf(resource.displayName().value()) : null;
  }

  @Override
  public Map<String, Parameter> getResolvedUriParameters() {
    if (resolvedUriParameters == null) {
      resolvedUriParameters = loadResolvedUriParameters(resource);
    }
    return resolvedUriParameters;
  }

  static Map<String, Parameter> loadResolvedUriParameters(org.raml.v2.api.model.v10.resources.Resource resource) {
    Map<String, Parameter> result = new HashMap<>();
    org.raml.v2.api.model.v10.resources.Resource current = resource;
    while (current != null) {
      for (TypeDeclaration typeDeclaration : current.uriParameters()) {
        result.put(typeDeclaration.name(), new ParameterImpl(typeDeclaration));
      }
      current = current.parentResource();
    }
    return result;
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
}
