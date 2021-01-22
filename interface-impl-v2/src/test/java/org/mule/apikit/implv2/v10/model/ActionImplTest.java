/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v10.model;

import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.implv2.ParserWrapperV2;
import org.mule.apikit.model.Resource;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ActionImplTest {

  private static final String GET_ACTION = "GET";
  private static final String PUT_ACTION = "PUT";
  private ActionImpl leaguesAction;
  private ActionImpl leagueIdAction;
  private ActionImpl teamsAction;

  @Before
  public void setUp() throws Exception {
    String apiLocation = this.getClass().getResource("/apis/10-leagues/api.raml").toURI().toString();
    RamlImpl10V2 parser = (RamlImpl10V2) new ParserWrapperV2(apiLocation, Collections.emptyList()).parse();
    Resource leaguesResource = parser.getResources().get("/leagues");
    leaguesAction = (ActionImpl) leaguesResource.getAction(GET_ACTION);
    Resource leagueIdResource = leaguesResource.getResources().get("/{leagueId}");
    leagueIdAction = (ActionImpl) leagueIdResource.getAction(PUT_ACTION);
    teamsAction =
        (ActionImpl) leagueIdResource.getResources().get("/teams").getAction(GET_ACTION);
  }

  @Test
  public void getTypeTest() {
    assertEquals(GET_ACTION, leaguesAction.getType().name());
  }

  @Test
  public void hasBodyTest() {
    assertFalse(leaguesAction.hasBody());
    assertTrue(leagueIdAction.hasBody());
  }

  @Test
  public void getResponsesTest() {
    assertEquals(1, leaguesAction.getResponses().size());
  }

  @Test
  public void getResourceTest() {
    assertEquals("Leagues", leaguesAction.getResource().getDisplayName());
  }

  @Test
  public void getBodyTest() {
    assertEquals(0, leaguesAction.getBody().size());
    assertEquals(2, leagueIdAction.getBody().size());
  }

  @Test
  public void getQueryParametersTest() {
    assertEquals(0, leaguesAction.getQueryParameters().size());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getBaseUriParametersTest() {
    leaguesAction.getBaseUriParameters();
  }

  @Test
  public void getResolvedUriParametersTest() {
    assertEquals(0, leaguesAction.getResolvedUriParameters().size());
  }

  @Test
  public void getHeadersTest() {
    assertEquals(0, leaguesAction.getHeaders().size());
    assertEquals(1, teamsAction.getHeaders().size());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getSecuredByTest() {
    leaguesAction.getSecuredBy();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getIsTest() {
    leaguesAction.getIs();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void cleanBaseUriParametersTest() {
    leaguesAction.cleanBaseUriParameters();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void setHeadersTest() {
    leaguesAction.setHeaders(Collections.emptyMap());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void setQueryParametersTest() {
    leaguesAction.setQueryParameters(Collections.emptyMap());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void setBodyTest() {
    leaguesAction.setBody(Collections.emptyMap());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void addResponseTest() {
    leaguesAction.addResponse(null, null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void addSecurityReferenceTest() {
    leaguesAction.addSecurityReference(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void addIsTest() {
    leaguesAction.addIs(null);
  }

  @Test
  public void queryStringTest() {
    assertNull(leaguesAction.queryString());
  }

  @Test
  public void getSuccessStatusCodeTest() {
    assertEquals("200", leaguesAction.getSuccessStatusCode());
    assertEquals("200", teamsAction.getSuccessStatusCode());
    assertEquals("204", leagueIdAction.getSuccessStatusCode());
  }
}
