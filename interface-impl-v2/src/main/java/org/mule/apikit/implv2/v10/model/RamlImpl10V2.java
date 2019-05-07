/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v10.model;

import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.SecurityScheme;
import org.mule.apikit.model.Template;
import org.mule.apikit.model.parameter.Parameter;

import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.api.model.v10.api.Api;
import org.raml.v2.api.model.v10.datamodel.AnyTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.ExternalTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.internal.utils.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.mule.apikit.common.RamlUtils.replaceBaseUri;
import static org.mule.apikit.implv2.ParserV2Utils.findIncludeNodes;
import static org.mule.apikit.implv2.ParserV2Utils.nullSafe;

public class RamlImpl10V2 implements ApiSpecification {

  private Api api;
  private final String ramlPath;
  private final ResourceLoader resourceLoader;

  public RamlImpl10V2(Api api, ResourceLoader resourceLoader, String ramlPath) {
    this.api = api;
    this.ramlPath = ramlPath;
    this.resourceLoader = resourceLoader;
  }

  @Override
  public Map<String, Resource> getResources() {
    Map<String, Resource> map = new LinkedHashMap<>();
    List<org.raml.v2.api.model.v10.resources.Resource> resources = api.resources();
    for (org.raml.v2.api.model.v10.resources.Resource resource : resources) {
      map.put(resource.relativeUri().value(), new ResourceImpl(resource));
    }
    return map;
  }

  @Override
  public String getBaseUri() {
    return nullSafe(api.baseUri());
  }

  @Override
  public String getVersion() {
    return nullSafe(api.version());
  }

  @Override
  public List<Map<String, String>> getSchemas() {
    Map<String, String> map = new LinkedHashMap<>();
    List<TypeDeclaration> types = api.types();
    if (types.isEmpty()) {
      types = api.schemas();
    }
    for (TypeDeclaration typeDeclaration : types) {
      map.put(typeDeclaration.name(), getTypeAsString(typeDeclaration));
    }
    List<Map<String, String>> result = new ArrayList<>();
    result.add(map);
    return result;
  }

  static String getTypeAsString(TypeDeclaration typeDeclaration) {
    if (typeDeclaration instanceof ExternalTypeDeclaration) {
      return ((ExternalTypeDeclaration) typeDeclaration).schemaContent();
    }
    if (typeDeclaration instanceof AnyTypeDeclaration) {
      return null;
    }
    //return non-null value in order to detect that a schema was defined
    return typeDeclaration.toJsonSchema();
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
  public List<String> getAllReferences() {
    try {
      return findIncludeNodes(getPathAsUri(ramlPath), resourceLoader);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return emptyList();
  }

  private URI getPathAsUri(String path) {
    final String normalizedPath = path.replace(File.separator, "/");
    return URI.create(normalizedPath);
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
