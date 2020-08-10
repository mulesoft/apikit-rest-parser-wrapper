/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v10.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.common.LazyValue;
import org.mule.apikit.implv2.ParserWrapperV2;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RamlImpl10V2Test {

  private static final String NEW_BASE_URI = "http://localhost/api/{version}";
  private static final String BASE_URI_PARAM = "apiDomain";
  private static final String BASE_URI = "https://{" + BASE_URI_PARAM + "}.ec2.amazonaws.com";
  private static final String APIS_10_LEAGUES_API_RAML_PATH = "/apis/10-leagues/api.raml";
  private static final String APIS_10_SCHEMAS_API_RAML_PATH = "/apis/10-schemas/api.raml";
  private RamlImpl10V2 api;

  @Before
  public void setUp() throws Exception {
    String apiLocation = this.getClass().getResource(APIS_10_LEAGUES_API_RAML_PATH).toURI().toString();
    api = (RamlImpl10V2) new ParserWrapperV2(apiLocation, new LazyValue<>(Collections::emptyList)).parse();
  }

  @Test
  public void getResourcesTest() {
    assertEquals(2, api.getResources().size());
  }

  @Test
  public void getBaseUriTest() {
    assertEquals(BASE_URI, api.getBaseUri());
  }

  @Test
  public void getLocationTest() {
    assertTrue(api.getLocation().endsWith(APIS_10_LEAGUES_API_RAML_PATH));
  }

  @Test
  public void getVersionTest() {
    assertEquals("v1", api.getVersion());
  }

  @Test
  public void getSchemasTest() throws Exception {
    assertEquals(1, api.getSchemas().size());
    assertTrue(api.getSchemas().get(0).containsKey("league-json"));
    assertTrue(api.getSchemas().get(0).containsKey("league-xml"));

    String apiLocation = this.getClass().getResource(APIS_10_SCHEMAS_API_RAML_PATH).toURI().toString();
    RamlImpl10V2 schemasParser = (RamlImpl10V2) new ParserWrapperV2(apiLocation, new LazyValue<>(Collections::emptyList)).parse();
    assertEquals(1, schemasParser.getSchemas().size());
    assertTrue(schemasParser.getSchemas().get(0).containsKey("jsonSchema"));
    assertTrue(schemasParser.getSchemas().get(0).containsKey("xmlSchema"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getResourceTest() {
    api.getResource("/leagues");//Check difference with amf parser
  }

  @Test
  public void getConsolidatedSchemasTest() {
    assertEquals(0, api.getConsolidatedSchemas().size());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getCompiledSchemasTest() {
    api.getCompiledSchemas();
  }

  @Test
  public void getBaseUriParametersTest() {
    assertEquals(1, api.getBaseUriParameters().size());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getSecuritySchemesTest() {
    api.getSecuritySchemes();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getTraitsTest() {
    api.getTraits();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getUriTest() {
    api.getUri();
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
    assertEquals("RAML_10", api.getApiVendor().name());
  }

  @Test
  public void dumpTest() {
    assertTrue(api.dump(NEW_BASE_URI).contains(NEW_BASE_URI));
    assertFalse(api.dump(StringUtils.EMPTY).contains(NEW_BASE_URI));
    assertFalse(api.dump(null).contains(NEW_BASE_URI));
  }
}
