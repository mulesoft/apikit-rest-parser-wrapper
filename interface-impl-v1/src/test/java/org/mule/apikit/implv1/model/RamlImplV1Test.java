/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.implv1.ParserWrapperV1;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RamlImplV1Test {

  private static final String TEAMS_RESOURCE = "/teams";
  private static final String BASE_URI_PARAM = "apiDomain";
  private static final String BASE_URI = "https://{" + BASE_URI_PARAM + "}.ec2.amazonaws.com";
  private static final String NEW_BASE_URI = "http://localhost/api/{version}";
  private static final String API_RESOURCE_PATH = "/apis/08-leagues/api.raml";
  private static final String EMPTY_API_RESOURCE_PATH = "/apis/08-empty/api.raml";
  private static final String API_VERSION = "1.0";
  private RamlImplV1 api;
  private RamlImplV1 emptyApi;

  @Before
  public void setUp() throws Exception {
    String apiLocation = this.getClass().getResource(API_RESOURCE_PATH).toURI().toString();
    api = (RamlImplV1) new ParserWrapperV1(apiLocation, Collections.emptyList()).parse();
    apiLocation = this.getClass().getResource(EMPTY_API_RESOURCE_PATH).toURI().toString();
    emptyApi = (RamlImplV1) new ParserWrapperV1(apiLocation, Collections.emptyList()).parse();
  }


  @Test
  public void getRamlTest() {
    assertEquals("La Liga", api.getRaml().getTitle());
  }


  @Test
  public void getInstanceTest() {
    assertNotNull(api.getInstance());
  }


  @Test
  public void getResourcesTest() {
    assertEquals(5, api.getResources().size());
    assertEquals(0, emptyApi.getResources().size());
  }

  @Test
  public void getBaseUriTest() {
    assertEquals(BASE_URI, api.getBaseUri());
    api.setBaseUri(StringUtils.EMPTY);
    assertTrue(api.getUri().isEmpty());
  }

  @Test
  public void getLocationTest() {
    assertTrue(api.getLocation().endsWith(API_RESOURCE_PATH));
  }

  @Test
  public void getVersionTest() {
    assertEquals(API_VERSION, api.getVersion());
  }

  @Test
  public void getSchemasTest() {
    assertEquals(0, api.getSchemas().size());
  }

  @Test
  public void getResourceTest() {
    assertNull(api.getResource("non-existent"));
    assertEquals(TEAMS_RESOURCE, api.getResource(TEAMS_RESOURCE).getUri());
  }

  @Test
  public void getConsolidatedSchemasTest() {
    assertEquals(0, api.getConsolidatedSchemas().size());
  }

  @Test
  public void getCompiledSchemasTest() {
    assertEquals(0, api.getCompiledSchemas().size());
  }

  @Test
  public void getBaseUriParametersTest() {
    assertEquals(1, api.getBaseUriParameters().size());
    assertEquals(0, emptyApi.getBaseUriParameters().size());
  }

  @Test
  public void getSecuritySchemesTest() {
    assertEquals(3, api.getSecuritySchemes().size());
    assertEquals(0, emptyApi.getSecuritySchemes().size());
  }

  @Test
  public void getTraitsTest() {
    assertEquals(1, api.getTraits().size());
    assertEquals(0, emptyApi.getTraits().size());
  }

  @Test
  public void getUriTest() {
    assertTrue(api.getUri().isEmpty());
  }

  @Test
  public void getAllReferencesTest() {
    assertEquals(0, api.getAllReferences().size());
  }

  @Test
  public void getTypeTest() {
    assertEquals("RAML", api.getType().name());
  }

  @Test
  public void getApiVendorTest() {
    assertEquals("RAML_08", api.getApiVendor().name());
  }

  @Test
  public void dumpTest() {
    assertTrue(api.dump(NEW_BASE_URI).contains(NEW_BASE_URI));
    assertFalse(api.dump(StringUtils.EMPTY).contains(NEW_BASE_URI));
    assertFalse(api.dump(null).contains(NEW_BASE_URI));
  }
}
