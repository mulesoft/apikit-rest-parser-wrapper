/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.environment.DefaultEnvironment;
import amf.client.environment.Environment;
import amf.client.execution.ExecutionEnvironment;
import amf.client.model.document.Document;
import amf.client.model.domain.EndPoint;
import amf.client.model.domain.Server;
import amf.client.model.domain.WebApi;
import amf.client.render.AmfGraphRenderer;
import amf.client.render.Oas20Renderer;
import amf.client.render.Raml08Renderer;
import amf.client.render.Raml10Renderer;
import amf.client.render.RenderOptions;
import amf.client.render.Renderer;
import org.mule.amf.impl.util.LazyValue;
import org.mule.apikit.ApiType;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.SecurityScheme;
import org.mule.apikit.model.Template;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.model.parameter.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.mule.apikit.ApiType.AMF;
import static org.mule.apikit.common.RamlUtils.replaceBaseUri;

public class AMFImpl implements ApiSpecification {

  private static final Logger LOGGER = LoggerFactory.getLogger(AMFImpl.class);
  private final WebApi webApi;
  private final Map<String, Map<String, Resource>> resources;
  private final List<String> references;
  private final ApiVendor apiVendor;
  private final transient LazyValue<Document> consoleModel;
  private final ApiReference apiRef;
  private final ExecutionEnvironment executionEnvironment;

  public AMFImpl(WebApi webApi, List<String> references, ApiVendor apiVendor, LazyValue<Document> console, ApiReference apiRef, ExecutionEnvironment executionEnvironment) {
    this.webApi = webApi;
    this.executionEnvironment = executionEnvironment;
    this.resources = buildResources(webApi.endPoints());
    this.references = references;
    this.apiVendor = apiVendor;
    this.consoleModel = console;
    this.apiRef = apiRef;
  }

  private Map<String, Map<String, Resource>> buildResources(final List<EndPoint> endPoints) {
    final Map<String, Map<String, Resource>> resources = new HashMap<>();
    endPoints.forEach(endPoint -> addToMap(resources, endPoint));
    return resources;
  }

  private void addToMap(final Map<String, Map<String, Resource>> resources, final EndPoint endPoint) {
    String parentKey = parentKey(endPoint);
    Map<String, Resource> parentMap = resources.computeIfAbsent(parentKey, k -> new LinkedHashMap<>());
    String childKey = endPoint.relativePath();
    parentMap.put(childKey, new ResourceImpl(this, endPoint, executionEnvironment));
  }

  private static String parentKey(final EndPoint endPoint) {
    final String path = endPoint.path().value();
    final String relativePath = endPoint.relativePath();
    return path.substring(0, path.length() - relativePath.length());
  }

  @Override
  public Resource getResource(final String path) {
    return getResources().get(path);
  }

  @Override
  public Map<String, String> getConsolidatedSchemas() {
    return null;
  }

  @Override
  public Map<String, Object> getCompiledSchemas() {
    return null;
  }

  @Override
  public String getBaseUri() {
    return getServer().map(server -> server.url().value()).orElse(null);
  }

  @Override
  public String getLocation() {
    return apiRef.getLocation();
  }

  private Optional<Server> getServer() {
    return webApi.servers().stream().findFirst();
  }


  @Override
  public Map<String, Resource> getResources() {
    return resources.getOrDefault("", emptyMap());
  }

  Map<String, Resource> getResources(final Resource resource) {
    final String key = resource.getUri();
    return resources.getOrDefault(key, emptyMap());
  }

  @Override
  public String getVersion() {
    return webApi.version().value();
  }

  @Override
  public Map<String, Parameter> getBaseUriParameters() {
    return getServer().<Map<String, Parameter>>map(server -> server.variables().stream()
      .collect(toMap(p -> p.name().value(), p -> new ParameterImpl(p,executionEnvironment))))
      .orElseGet(Collections::emptyMap);
  }

  @Override
  public List<Map<String, SecurityScheme>> getSecuritySchemes() {
    return null;
  }

  @Override
  public List<Map<String, Template>> getTraits() {
    return null;
  }

  @Override
  public String getUri() {
    Option<String> location = webApi._internal().location();
    return location.isDefined() ? location.get() : null;
  }

  @Override
  public String dump(String newBaseUri) {
    String dump = renderApi();
    if (newBaseUri != null) {
      dump = replaceBaseUri(dump, newBaseUri);
    }
    return dump;
  }

  @Override
  public ApiVendor getApiVendor() {
    return apiVendor;
  }

  @Override
  public ApiType getType() {
    return AMF;
  }

  private String renderApi() {
    Environment env = DefaultEnvironment.apply(executionEnvironment);
    Renderer renderer;
    switch (apiVendor) {
      case RAML_08:
        renderer = new Raml08Renderer(env);
        break;
      case OAS_20:
        renderer = new Oas20Renderer(env);
        break;
      default:
        renderer = new Raml10Renderer(env);
        break;
    }
    try {
      return renderer.generateString(consoleModel.get()).get();
    } catch (final InterruptedException | ExecutionException e) {
      LOGGER.error(format("Error render API '%s' to '%s'", apiRef.getLocation(), apiVendor.name()), e);
      return "";
    }
  }

  @Override
  public List<Map<String, String>> getSchemas() {
    return emptyList();
  }

  @Override
  public List<String> getAllReferences() {
    return references;
  }

  // This method should only be used by API Console... /shrug
  public String dumpAmf() {
    try {
      return new AmfGraphRenderer(DefaultEnvironment.apply(executionEnvironment)).generateString(consoleModel.get(),
          new RenderOptions()
              .withoutSourceMaps()
              .withoutPrettyPrint()
              .withCompactUris()).get();
    } catch (InterruptedException | ExecutionException e) {
      return e.getMessage();
    }
  }

  public void updateBaseUri(String baseUri) {
    if (webApi.servers() != null && webApi.servers().size() > 0) {
      final Server server = webApi.servers().get(0);
      server.withUrl(baseUri);
      server.withVariables(emptyList());
    } else {
      webApi.withServer(baseUri);
    }
    consoleModel.get().withEncodes(webApi);
  }
}
