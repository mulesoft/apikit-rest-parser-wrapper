/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.model;

import org.apache.commons.collections.MapUtils;
import org.mule.apikit.implv1.model.parameter.ParameterImpl;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.ActionType;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.parameter.Parameter;
import org.raml.model.parameter.UriParameter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mule.apikit.ParserUtils.resolveVersion;

public class ResourceImpl implements Resource {

  private static final String VERSION = "version";
  org.raml.model.Resource resource;

  public ResourceImpl(org.raml.model.Resource resource) {
    this.resource = resource;
  }

  @Override
  public Action getAction(String s) {
    org.raml.model.Action action = resource.getAction(s);
    if (action == null) {
      return null;
    }
    return new ActionImpl(action);

  }

  @Override
  public String getUri() {
    return resource.getUri();
  }

  @Override
  public String getResolvedUri(String version) {
    return resolveVersion(getUri(), version);
  }

  @Override
  public void setParentUri(String s) {
    resource.setParentUri(s);
  }

  @Override
  public Map<String, Resource> getResources() {
    if (resource.getResources() == null) {
      return null;
    }
    Map<String, Resource> map = new LinkedHashMap<>();
    for (Map.Entry<String, org.raml.model.Resource> entry : resource.getResources().entrySet()) {
      map.put(entry.getKey(), new ResourceImpl(entry.getValue()));
    }
    return map;
  }

  @Override
  public String getParentUri() {
    return resource.getParentUri();
  }

  @Override
  public Map<ActionType, Action> getActions() {
    if (resource.getActions() == null) {
      return null;
    }
    Map<ActionType, Action> map = new LinkedHashMap<>();
    for (Map.Entry<org.raml.model.ActionType, org.raml.model.Action> entry : resource.getActions().entrySet()) {
      map.put(ActionType.valueOf(entry.getKey().name()), new ActionImpl(entry.getValue()));
    }
    return map;
  }

  @Override
  public Map<String, List<Parameter>> getBaseUriParameters() {
    if (resource.getBaseUriParameters() == null) {
      return null;
    }
    Map<String, List<Parameter>> map = new LinkedHashMap<>();
    for (Map.Entry<String, List<UriParameter>> entry : resource.getBaseUriParameters().entrySet()) {
      List<Parameter> list = new ArrayList<>();
      for (UriParameter parameter : entry.getValue()) {
        list.add(new ParameterImpl(parameter));
      }
      map.put(entry.getKey(), list);
    }
    return map;
  }

  @Override
  public Map<String, Parameter> getResolvedUriParameters() {
    return loadResolvedUriParameters(resource);
  }

  static Map<String, Parameter> loadResolvedUriParameters(org.raml.model.Resource resource) {
    if (resource.getResolvedUriParameters() == null) {
      return null;
    }
    Map<String, Parameter> map = new LinkedHashMap<>();
    for (Map.Entry<String, UriParameter> entry : resource.getResolvedUriParameters().entrySet()) {
      if (!VERSION.equals(entry.getKey())) {
        map.put(entry.getKey(), new ParameterImpl(entry.getValue()));
      }
    }
    Map<String, List<UriParameter>> baseUriParameters = resource.getBaseUriParameters();
    if (MapUtils.isNotEmpty(baseUriParameters)) {
      for (Map.Entry<String, List<UriParameter>> entry : baseUriParameters.entrySet()) {
        map.put(entry.getKey(), new ParameterImpl(entry.getValue().get(0)));
      }
    }
    return map;
  }

  @Override
  public String getDisplayName() {
    return resource.getDisplayName();
  }

  @Override
  public String getRelativeUri() {
    return resource.getRelativeUri();
  }

  @Override
  public void cleanBaseUriParameters() {
    resource.getBaseUriParameters().clear();
  }

  @Override
  public String toString() {
    return getUri();
  }
}
