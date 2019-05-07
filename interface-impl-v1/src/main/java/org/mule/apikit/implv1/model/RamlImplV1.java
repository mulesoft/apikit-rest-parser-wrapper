/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.model;

import org.mule.apikit.implv1.ParserV1Utils;
import org.mule.apikit.implv1.model.parameter.ParameterImpl;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.SecurityScheme;
import org.mule.apikit.model.Template;
import org.mule.apikit.model.parameter.Parameter;

import org.apache.commons.beanutils.BeanUtils;
import org.raml.emitter.RamlEmitter;
import org.raml.model.Raml;
import org.raml.model.parameter.UriParameter;
import org.raml.parser.loader.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public class RamlImplV1 implements ApiSpecification {

  private Raml raml;
  private ResourceLoader resourceLoader;
  private String ramlPath;
  private Logger logger;

  public RamlImplV1(Raml raml, ResourceLoader resourceLoader, String ramlPath) {
    this.raml = raml;
    this.resourceLoader = resourceLoader;
    this.ramlPath = ramlPath;
    this.logger = LoggerFactory.getLogger(RamlImplV1.class);
  }

  public RamlImplV1(Raml raml) {
    this.raml = raml;
  }

  public Raml getRaml() {
    return raml;
  }

  public Resource getResource(String s) {
    org.raml.model.Resource resource = raml.getResource(s);
    if (resource == null) {
      return null;
    }
    return new ResourceImpl(resource);
  }

  public Map<String, String> getConsolidatedSchemas() {
    return raml.getConsolidatedSchemas();
  }

  public Map<String, Object> getCompiledSchemas() {
    return raml.getCompiledSchemas();
  }

  public String getBaseUri() {
    return raml.getBaseUri();
  }

  public Map<String, Resource> getResources() {
    if (raml.getResources() == null) {
      return null;
    }
    Map<String, Resource> map = new LinkedHashMap<>();
    for (Map.Entry<String, org.raml.model.Resource> entry : raml.getResources().entrySet()) {
      map.put(entry.getKey(), new ResourceImpl(entry.getValue()));
    }
    return map;
  }

  public String getVersion() {
    return raml.getVersion();
  }

  public void setBaseUri(String s) {
    raml.setBaseUri(s);
  }

  public Map<String, Parameter> getBaseUriParameters() {
    if (raml.getBaseUriParameters() == null) {
      return emptyMap();
    }
    Map<String, Parameter> map = new LinkedHashMap<>();
    for (Map.Entry<String, UriParameter> entry : raml.getBaseUriParameters().entrySet()) {
      map.put(entry.getKey(), new ParameterImpl(entry.getValue()));
    }
    return map;
  }

  public List<Map<String, SecurityScheme>> getSecuritySchemes() {
    if (raml.getSecuritySchemes() == null) {
      return null;
    }
    List<Map<String, SecurityScheme>> list = new ArrayList<>();
    for (Map<String, org.raml.model.SecurityScheme> map : raml.getSecuritySchemes()) {
      Map<String, SecurityScheme> newMap = new LinkedHashMap<>();
      for (Map.Entry<String, org.raml.model.SecurityScheme> entry : map.entrySet()) {
        newMap.put(entry.getKey(), new SecuritySchemeImpl(entry.getValue()));
      }
      list.add(newMap);
    }
    return list;
  }

  public List<Map<String, Template>> getTraits() {
    if (raml.getTraits() == null) {
      return null;
    }
    List<Map<String, Template>> list = new ArrayList<>();
    for (Map<String, org.raml.model.Template> map : raml.getTraits()) {
      Map<String, Template> newMap = new LinkedHashMap<>();
      for (Map.Entry<String, org.raml.model.Template> entry : map.entrySet()) {
        newMap.put(entry.getKey(), new TemplateImpl(entry.getValue()));
      }
      list.add(newMap);
    }
    return list;
  }

  public String getUri() {
    return raml.getUri();
  }

  public List<Map<String, String>> getSchemas() {
    return raml.getSchemas();
  }

  public Object getInstance() {
    return raml;
  }

  @Override
  public List<String> getAllReferences() {
    try {
      return ParserV1Utils.detectIncludes(getPathAsUri(ramlPath), resourceLoader);
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
    return emptyList();
  }

  @Override
  public String dump(String newBaseUri) {
    RamlEmitter emitter = new RamlEmitter();
    if (newBaseUri != null) {
      try {
        Raml clone = (Raml) BeanUtils.cloneBean(raml);
        clone.setBaseUri(newBaseUri);
        return emitter.dump(clone);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return emitter.dump(raml);
  }

  private URI getPathAsUri(String path) {
    final String normalizedPath = path.replace(File.separator, "/");
    return URI.create(normalizedPath);
  }
}
