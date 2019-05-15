/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v08.model;

import static java.util.Collections.emptyMap;
import static org.mule.apikit.ApiType.RAML;
import static org.mule.apikit.common.RamlUtils.replaceBaseUri;

import org.mule.apikit.ApiType;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.SecurityScheme;
import org.mule.apikit.model.Template;
import org.mule.apikit.model.parameter.Parameter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.api.model.v08.api.Api;
import org.raml.v2.api.model.v08.api.GlobalSchema;
import org.raml.v2.internal.utils.StreamUtils;

public class RamlImpl08V2 implements ApiSpecification {

  private Api api;
  private final ResourceLoader resourceLoader;
  private final String ramlPath;

  public RamlImpl08V2(Api api) {
    this(api, null, null);
  }

  public RamlImpl08V2(Api api, ResourceLoader resourceLoader, String ramlPath) {
    this.api = api;
    this.resourceLoader = resourceLoader;
    this.ramlPath = ramlPath;
  }

  @Override
  public Map<String, Resource> getResources() {
    Map<String, Resource> map = new LinkedHashMap<>();
    List<org.raml.v2.api.model.v08.resources.Resource> resources = api.resources();
    for (org.raml.v2.api.model.v08.resources.Resource resource : resources) {
      map.put(resource.relativeUri().value(), new ResourceImpl(resource));
    }
    return map;
  }

  @Override
  public String getVersion() {
    return api.version();
  }

  @Override
  public Resource getResource(String path) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, String> getConsolidatedSchemas() {
    return emptyMap();
  }

  @Override
  public Map<String, Object> getCompiledSchemas() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getBaseUri() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getLocation() {
    return ramlPath;
  }

  @Override
  public Map<String, Parameter> getBaseUriParameters() {
    final Map<String, Parameter> baseUriParameters = new LinkedHashMap<>();

    api.baseUriParameters().forEach(type -> baseUriParameters.put(type.name(), new ParameterImpl(type)));

    return baseUriParameters;
  }

  @Override
  public List<Map<String, SecurityScheme>> getSecuritySchemes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Map<String, Template>> getTraits() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getUri() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Map<String, String>> getSchemas() {
    Map<String, String> map = new LinkedHashMap<>();
    List<GlobalSchema> schemas = api.schemas();
    for (GlobalSchema schema : schemas) {
      map.put(schema.key(), schema.value() != null ? schema.value().value() : null);
    }
    List<Map<String, String>> result = new ArrayList<>();
    result.add(map);
    return result;
  }

  @Override
  public ApiVendor getApiVendor() {
    return ApiVendor.RAML_10;
  }

  @Override
  public ApiType getType() {
    return RAML;
  }

  @Override
  public List<String> getAllReferences() {
    return Collections.emptyList();
  }

  @Override
  public String dump(String newBaseUri) {
    InputStream stream = resourceLoader.fetchResource(ramlPath);
    if (stream != null) {
      String raml = StreamUtils.toString(stream);
      if (newBaseUri != null) {
        return replaceBaseUri(raml, newBaseUri);
      }
      return raml;
    }
    throw new RuntimeException("Invalid RAML descriptor");
  }
}
