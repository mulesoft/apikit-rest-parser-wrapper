/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.model;

import org.mule.apikit.model.parameter.Parameter;

import java.util.List;
import java.util.Map;

public interface Resource {

  Action getAction(String name);

  String getUri();

  String getResolvedUri(String version);

  void setParentUri(String parentUri);

  Map<String, Resource> getResources();

  String getParentUri();

  Map<ActionType, Action> getActions();

  Map<String, List<Parameter>> getBaseUriParameters();

  Map<String, Parameter> getResolvedUriParameters();

  String getDisplayName();

  String getRelativeUri();

  void cleanBaseUriParameters();
}
