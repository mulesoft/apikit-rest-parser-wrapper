/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mule.amf.impl.AMFParser;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.api.ApiReference;

import java.io.File;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AMFImplTest {

  private static final String BASE_URI = "some.uri.com";
  private static final String RESOURCE = "/test";
  private static final String ACTION = "GET";
  private static final String LEAGUES_RESOURCE = "/leagues";
  private static final String LEAGUES_API_BASE_URI = "https://{apiDomain}.ec2.amazonaws.com";
  private static final String LEAGUES_API_VERSION = "v1";

  private URI apiLocation;
  private AMFImpl api;

  @Before
  public void setUp() throws Exception {
    apiLocation = AMFImplTest.class.getResource("../10-leagues/api.raml").toURI();
    ApiReference apiRef = ApiReference.create(apiLocation.toString());
    api = (AMFImpl) new AMFParser(apiRef, true).parse();
  }

  @Test
  public void amfImplSimpleApiCompleteTest() throws Exception {
    String apiLocation = AMFImplTest.class.getResource("../amf-model-render/api-to-render.raml").toURI().toString();
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
    assertEquals(2, resource.getActions().size());
    assertSimpleApiAction(resource.getAction(ACTION));
  }

  private void assertSimpleApiAction(Action action) {
    assertEquals(0, action.getBody().size());
    Assert.assertFalse(action.hasBody());
    assertEquals(1, action.getResponses().size());
    assertEquals(RESOURCE, action.getResource().toString());
    assertEquals(0, action.getQueryParameters().size());
    assertEquals(1, action.getHeaders().size());
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
    assertTrue(api.getLocation().endsWith("10-leagues/api.raml".replace("/", File.separator)));
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
    String location = AMFImplTest.class.getResource("../oas/api.yaml").toURI().toString();
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

}
