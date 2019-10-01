/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.model;

import static java.util.Collections.emptyMap;
import static org.mule.apikit.ApiType.RAML;
import static org.mule.apikit.model.ApiVendor.RAML_08;

import org.mule.apikit.ApiType;
import org.mule.apikit.common.ApiSyncUtils;
import org.mule.apikit.implv1.model.parameter.ParameterImpl;
import org.mule.apikit.implv1.parser.Raml08ReferenceFinder;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.SecurityScheme;
import org.mule.apikit.model.Template;
import org.mule.apikit.model.parameter.Parameter;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.raml.emitter.RamlEmitter;
import org.raml.model.Raml;
import org.raml.model.parameter.UriParameter;
import org.raml.parser.loader.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RamlImplV1 implements ApiSpecification {

  private Raml raml;
  private String ramlPath;
  private final Raml08ReferenceFinder referenceFinder;
  private List<String> references;

  public RamlImplV1(Raml raml, ResourceLoader resourceLoader, String ramlPath, List<String> references) {
    this.raml = raml;
    this.ramlPath = ramlPath;
    this.referenceFinder = new Raml08ReferenceFinder(resourceLoader);
    this.references = references;
  }

  @Deprecated
  public RamlImplV1(Raml raml, Raml08ReferenceFinder referenceFinder) {
    this.raml = raml;
    this.referenceFinder = referenceFinder;
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

  @Override
  public String getLocation() {
    return ramlPath;
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

  /**
   * This method returns a list of String with all the references from the api.
   * Note that the list returned by this method is based on the ramlPath, so if
   * for e.g: the path described at ramlPath is relative the list returned will
   * be of relatives path too, same if it absolute path or uri.
   *
   * @return list of String with all the references from the api
   */
  @Override
  public List<String> getAllReferences() {
    return references;
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

  @Override
  public ApiVendor getApiVendor() {
    return RAML_08;
  }

  @Override
  public ApiType getType() {
    return RAML;
  }
}
