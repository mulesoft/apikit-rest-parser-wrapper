/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v10.model;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.common.LazyValue;
import org.mule.apikit.implv2.ParserWrapperV2;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.ActionType;
import org.mule.apikit.model.Resource;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ResourceImplTest {

  private static final String API_VERSION = "v1";
  private static final String LEAGUES_RESOURCE = "/leagues";
  private static final String LEAGUE_ID_RESOURCE = "/{leagueId}";
  private static final String LEAGUES_HISTORY_RESOURCE = "/history/{version}";
  private static final String LEAGUES_HISTORY_RESOLVED_URI = "/history/" + API_VERSION;
  private static final String GET_ACTION = "GET";
  private static final String POST_ACTION = "POST";
  private static final String PUT_ACTION = "PUT";
  private Resource leaguesResource;
  private Resource leagueIdResource;
  private Resource leaguesHistoryResource;

  @Before
  public void setUp() throws Exception {
    String apiLocation = this.getClass().getResource("/apis/10-leagues/api.raml").toURI().toString();
    RamlImpl10V2 parser = (RamlImpl10V2) new ParserWrapperV2(apiLocation, new LazyValue<>(Collections::emptyList)).parse();
    Map<String, Resource> resources = parser.getResources();
    leaguesResource = resources.get(LEAGUES_RESOURCE);
    leagueIdResource = leaguesResource.getResources().get(LEAGUE_ID_RESOURCE);
    leaguesHistoryResource = resources.get(LEAGUES_HISTORY_RESOURCE);
  }

  @Test
  public void getRelativeUriTest() {
    assertEquals(LEAGUES_RESOURCE, leaguesResource.getRelativeUri());
    assertEquals(LEAGUE_ID_RESOURCE, leagueIdResource.getRelativeUri());
    assertEquals(LEAGUES_HISTORY_RESOURCE, leaguesHistoryResource.getRelativeUri());
  }

  @Test
  public void getUriTest() {
    assertEquals(LEAGUES_RESOURCE, leaguesResource.getUri());
    assertEquals(LEAGUES_RESOURCE + LEAGUE_ID_RESOURCE, leagueIdResource.getUri());
    assertEquals(LEAGUES_HISTORY_RESOURCE, leaguesHistoryResource.getUri());
  }

  @Test
  public void getResolvedUriTest() {
    assertEquals(LEAGUES_RESOURCE, leaguesResource.getResolvedUri(API_VERSION));
    assertEquals(LEAGUES_RESOURCE + LEAGUE_ID_RESOURCE, leagueIdResource.getResolvedUri(API_VERSION));
    assertEquals(LEAGUES_HISTORY_RESOLVED_URI, leaguesHistoryResource.getResolvedUri(API_VERSION));
  }

  @Test
  public void getParentUriTest() {
    assertEquals(StringUtils.EMPTY, leaguesResource.getParentUri());
    assertEquals(LEAGUES_RESOURCE, leagueIdResource.getParentUri());
    assertEquals(StringUtils.EMPTY, leaguesHistoryResource.getParentUri());
  }

  @Test
  public void getActionTest() {
    assertEquals(ActionType.GET, leaguesResource.getAction(GET_ACTION).getType());
    assertEquals(ActionType.POST, leaguesResource.getAction(POST_ACTION).getType());
    assertNull(leaguesResource.getAction(PUT_ACTION));
  }

  @Test
  public void getActionsTest() {
    Map<ActionType, Action> actions = leaguesResource.getActions();
    assertEquals(2, actions.size());
    assertNotNull(actions.get(ActionType.GET));
    assertNotNull(actions.get(ActionType.POST));
    assertNull(actions.get(ActionType.PUT));
    actions = leagueIdResource.getActions();
    assertEquals(3, actions.size());
    assertNotNull(actions.get(ActionType.GET));
    assertNull(actions.get(ActionType.POST));
    assertNotNull(actions.get(ActionType.PUT));
  }

  @Test
  public void getResourcesTest() {
    Map<String, Resource> resources = leaguesResource.getResources();
    assertEquals(1, resources.size());
    resources = leagueIdResource.getResources();
    assertEquals(2, resources.size());
  }

  @Test
  public void getDisplayNameTest() {
    assertEquals("Leagues", leaguesResource.getDisplayName());
    assertEquals("Leagues History", leaguesHistoryResource.getDisplayName());
    assertEquals(LEAGUE_ID_RESOURCE, leagueIdResource.getDisplayName());
  }

  @Test
  public void getResolvedUriParametersTest() {
    assertEquals(1, leagueIdResource.getResolvedUriParameters().size());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void setParentUriTest() {
    leaguesResource.setParentUri("/api");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getBaseUriParametersTest() {
    leaguesResource.getBaseUriParameters();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void cleanBaseUriParametersTest() {
    leaguesResource.cleanBaseUriParameters();
  }

}
