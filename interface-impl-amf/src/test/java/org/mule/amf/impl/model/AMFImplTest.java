/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import amf.apicontract.client.platform.AMFBaseUnitClient;
import amf.apicontract.client.platform.APIConfiguration;
import amf.core.client.platform.config.RenderOptions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mule.amf.impl.AMFParser;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.api.ApiReference;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AMFImplTest {

  private static final String BASE_URI = "some.uri.com";
  private static final String RESOURCE = "/test";
  private static final String ACTION = "POST";
  private static final String LEAGUES_RESOURCE = "/leagues";
  private static final String LEAGUES_API_BASE_URI = "https://{apiDomain}.ec2.amazonaws.com";
  private static final String LEAGUES_API_VERSION = "v1";

  private URI apiLocation;
  private AMFImpl api;

  @Before
  public void setUp() throws Exception {
    apiLocation = AMFImplTest.class.getResource("../leagues/raml10/api.raml").toURI();
    ApiReference apiRef = ApiReference.create(apiLocation.toString());
    api = (AMFImpl) new AMFParser(apiRef, true).parse();
  }

  @Test
  public void amfImplSimpleApiCompleteTest() throws Exception {
    String apiLocation = AMFImplTest.class.getResource("../amf-model-render/raml/api-to-render.raml").toURI().toString();
    ApiReference apiRef = ApiReference.create(apiLocation);
    AMFImpl simpleApi = (AMFImpl) new AMFParser(apiRef, true).parse();

    assertNull(simpleApi.getSecuritySchemes());
    assertNull(simpleApi.getTraits());
    simpleApi.updateBaseUri(BASE_URI);
    assertEquals(BASE_URI, simpleApi.getBaseUri());

    ResourceImpl resource = (ResourceImpl) simpleApi.getResource(RESOURCE);
    assertSimpleApiResource(resource);
  }

  private void assertSimpleApiResource(ResourceImpl resource) {
    assertEquals(RESOURCE, resource.toString());
    assertEquals(1, resource.getActions().size());
    assertSimpleApiAction(resource.getAction(ACTION));
  }

  private void assertSimpleApiAction(Action action) {
    Assert.assertTrue(action.hasBody());
    assertEquals(1, action.getBody().size());
    assertEquals(0, action.getResponses().size());
    assertEquals(RESOURCE, action.getResource().toString());
    assertEquals(1, action.getQueryParameters().size());
    assertEquals(0, action.getHeaders().size());
    assertNull(action.queryString());
  }


  @Test
  public void getResourceTest() {
    Resource resource = api.getResource(LEAGUES_RESOURCE);
    assertNotNull(resource);
    assertEquals(LEAGUES_RESOURCE, resource.getUri());
  }

  @Test
  public void getConsolidatedSchemasTest() {
    assertNull(api.getConsolidatedSchemas());
  }

  @Test
  public void getCompiledSchemasTest() {
    assertNull(api.getCompiledSchemas());
  }

  @Test
  public void getBaseUriTest() {
    assertEquals(LEAGUES_API_BASE_URI, api.getBaseUri());
  }

  @Test
  public void getLocationTest() {
    assertTrue(api.getLocation().endsWith("org/mule/amf/impl/leagues/raml10/api.raml".replace("/", File.separator)));
  }

  @Test
  public void getResourcesTest() {
    assertEquals(2, api.getResources().size());
  }

  @Test
  public void getVersionTest() {
    assertEquals(LEAGUES_API_VERSION, api.getVersion());
  }

  @Test
  public void getBaseUriParametersTest() {
    assertEquals(1, api.getBaseUriParameters().size());
  }

  @Test
  public void getSecuritySchemesTest() {
    assertNull(api.getSecuritySchemes());
  }

  @Test
  public void getTraitsTest() {
    assertNull(api.getTraits());
  }

  @Test
  public void getUriTest() {
    assertTrue(api.getUri().contains(apiLocation.getRawPath()));
  }

  @Test
  public void dumpTest() throws Exception {
    assertTrue(api.dump(BASE_URI).contains(BASE_URI));
    assertFalse(api.dump(StringUtils.EMPTY).contains(BASE_URI));
    assertFalse(api.dump(null).contains(BASE_URI));

    // Test OAS dump but there is not basePath replacement so far
    String location = AMFImplTest.class.getResource("../oas20-petstore/api.yaml").toURI().toString();
    ApiReference apiRef = ApiReference.create(location);
    AMFImpl oasApi = (AMFImpl) new AMFParser(apiRef, true).parse();
    assertFalse(oasApi.dump(BASE_URI).contains(BASE_URI));
  }

  @Test
  public void getApiVendorTest() {
    assertEquals("RAML_10", api.getApiVendor().name());
  }

  @Test
  public void getTypeTest() {
    assertEquals("AMF", api.getType().name());
  }

  @Test
  public void getSchemasTest() {
    assertTrue(api.getSchemas().isEmpty());
  }

  @Test
  public void getAllReferencesTest() {
    assertEquals(2, api.getAllReferences().size());
  }

  @Test
  public void dumpAmfTest() {
    assertNotNull(api.dumpAmf());
  }

  @Test
  public void updateBaseUriTest() {
    assertEquals(LEAGUES_API_BASE_URI, api.getBaseUri());
    api.updateBaseUri(BASE_URI);
    assertEquals(BASE_URI, api.getBaseUri());
  }

  @Test
  public void streamAMFModel() throws IOException {
    writeAndAssertBaseUri(LEAGUES_API_BASE_URI);
  }

  @Test
  public void updateBaseUriInStreamedAMFModelTest() throws IOException {
    api.updateBaseUri(BASE_URI);
    writeAndAssertBaseUri(BASE_URI);
  }

  @Test
  public void updateBaseUriInDumpedAMFModelTest() {
    assertThatBaseUriIsPresent(api.dumpAmf(), LEAGUES_API_BASE_URI);
    api.updateBaseUri(BASE_URI);
    assertThatBaseUriIsPresent(api.dumpAmf(), BASE_URI);
  }

  private void writeAndAssertBaseUri(String baseUri) throws IOException {
    PipedOutputStream pipedOutputStream = new PipedOutputStream();
    PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
    Thread thread = new Thread(() -> api.writeAMFModel(pipedOutputStream));
    thread.start();
    String model = IOUtils.toString(pipedInputStream, StandardCharsets.UTF_8);
    assertThatBaseUriIsPresent(model, baseUri);
  }

  private void assertThatBaseUriIsPresent(String model, String baseUri) {
    assertThat(model, containsString(baseUri));
  }

}
