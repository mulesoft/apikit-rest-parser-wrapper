/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.apicontract.client.platform.model.domain.EndPoint;
import amf.apicontract.client.platform.model.domain.Server;
import amf.apicontract.client.platform.model.domain.api.WebApi;
import amf.core.client.platform.model.document.Document;
import org.mule.amf.impl.parser.factory.AMFParserWrapper;
import org.mule.amf.impl.util.LazyValue;
import org.mule.apikit.ApiType;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.SecurityScheme;
import org.mule.apikit.model.Template;
import org.mule.apikit.model.parameter.Parameter;
import org.mulesoft.common.io.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.builder.JsonOutputBuilder;
import scala.Option;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.mule.apikit.ApiType.AMF;
import static org.mule.apikit.common.RamlUtils.replaceBaseUri;

public class AMFImpl implements ApiSpecification {

  private static final Logger LOGGER = LoggerFactory.getLogger(AMFImpl.class);
  private final WebApi webApi;
  private final Map<String, Map<String, Resource>> resources;
  private final List<String> references;
  private final ApiVendor apiVendor;
  private final transient LazyValue<Document> consoleModel;
  private final String apiLocation;
  private final AMFParserWrapper parser;

  public AMFImpl(WebApi webApi, List<String> references, AMFParserWrapper parser, ApiVendor vendor, String location, URI uri) {
    this.webApi = webApi;
    this.parser = parser;
    this.resources = buildResources(webApi.endPoints());
    this.references = references;
    this.apiVendor = vendor;
    this.apiLocation = location;
    this.consoleModel = new LazyValue<>(parser::parseApi);
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
    parentMap.put(childKey, new ResourceImpl(this, endPoint, parser.getApiConfiguration()));
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
    return apiLocation;
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
        .collect(toMap(p -> p.name().value(), p -> new ParameterImpl(p, parser.getApiConfiguration()))))
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
    return parser.renderApi(consoleModel.get());
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
    return parser.renderApi(consoleModel.get());
  }

  public void writeAMFModel(OutputStream outputStream) {
    try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"))) {
      parser.renderApi(consoleModel.get(), new JsonOutputBuilder<>(writer, false,
              Output.outputWriter()));
    } catch (IOException e) {
      throw new RuntimeException("Error trying to dump AMF model", e);
    }
  }

  /**
   * Updates both webApi's base URI, the one for validation and the other one for the console.
   *
   * @param baseUri new base URI value
   */
  public void updateBaseUri(String baseUri) {
    updateBaseUri(baseUri, webApi);
    updateBaseUri(baseUri, (WebApi) consoleModel.get().encodes());
  }

  private void updateBaseUri(String baseUri, WebApi webApi) {
    if (webApi.servers() != null && webApi.servers().size() > 0) {
      final Server server = webApi.servers().get(0);
      server.withUrl(baseUri);
      server.withVariables(emptyList());
    } else {
      webApi.withServer(baseUri);
    }
  }

  public boolean includesCallbacks() {
    return webApi.endPoints().stream().flatMap(endPoint -> endPoint.operations().stream())
        .anyMatch(operation -> isNotEmpty(operation.callbacks()));
  }

}
