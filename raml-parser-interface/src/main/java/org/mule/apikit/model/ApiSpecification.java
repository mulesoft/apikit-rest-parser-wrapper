/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.model;

import org.mule.apikit.model.parameter.Parameter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface ApiSpecification extends Serializable {

  Resource getResource(String path);

  Map<String, String> getConsolidatedSchemas();

  Map<String, Object> getCompiledSchemas();// TODO THIS MUST BE REMOVED

  String getBaseUri();

  Map<String, Resource> getResources();

  String getVersion();

  Map<String, Parameter> getBaseUriParameters();

  List<Map<String, SecurityScheme>> getSecuritySchemes();

  List<Map<String, Template>> getTraits();

  String getUri();

  List<Map<String, String>> getSchemas();

  List<String> getAllReferences();

  String dump(String newBaseUri);
}
