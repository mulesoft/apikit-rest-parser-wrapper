/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.apikit.implv1.model;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.implv1.ParserWrapperV1;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ResourceImplTest {

  private static final String API_VERSION = "v1";
  private static final String parentUri = "http://localhots:8081";

  private static final String TEAMS_RESOURCE = "/teams";
  private static final String TEAM_ID_RESOURCE = "/{teamId}";
  private static final String TEAMS_HISTORY_RESOURCE = "/history/{version}";
  private static final String TEAMS_HISTORY_RESOLVED_URI = "/history/" + API_VERSION + "/{year}";
  private static final String TEAMS_HISTORY_YEAR_RESOURCE = "/{year}";
  private static final String GET_ACTION = "GET";
  private static final String PUT_ACTION = "PUT";

  private ResourceImpl resource;
  private ResourceImpl resourceWithUriParams;
  private ResourceImpl resourceWithBaseUriParam;

  @Before
  public void setUp() throws Exception {
    String apiLocation = this.getClass().getResource("/apis/08-leagues/api.raml").toURI().toString();
    RamlImplV1 api = (RamlImplV1) new ParserWrapperV1(apiLocation, Collections.emptyList()).parse();
    resource = (ResourceImpl) api.getResource(TEAMS_RESOURCE);
    resourceWithUriParams = (ResourceImpl) api.getResource(TEAMS_RESOURCE).getResources().get(TEAM_ID_RESOURCE);
    resourceWithBaseUriParam =
        (ResourceImpl) api.getResource(TEAMS_HISTORY_RESOURCE).getResources().get(TEAMS_HISTORY_YEAR_RESOURCE);
  }

  @Test
  public void getActionTest() {
    assertNull(resource.getAction(PUT_ACTION));
    assertNotNull(resource.getAction(GET_ACTION));
  }

  @Test
  public void getUriTest() {
    assertEquals(TEAMS_RESOURCE, resource.getUri());
    assertEquals(TEAMS_RESOURCE + TEAM_ID_RESOURCE, resourceWithUriParams.getUri());
    assertEquals(TEAMS_HISTORY_RESOURCE + TEAMS_HISTORY_YEAR_RESOURCE, resourceWithBaseUriParam.getUri());
  }

  @Test
  public void getResolvedUriTest() {
    assertEquals(TEAMS_RESOURCE, resource.getResolvedUri(API_VERSION));
    assertEquals(TEAMS_RESOURCE + TEAM_ID_RESOURCE, resourceWithUriParams.getResolvedUri(API_VERSION));
    assertEquals(TEAMS_HISTORY_RESOLVED_URI, resourceWithBaseUriParam.getResolvedUri(API_VERSION));
  }

  @Test
  public void getResourcesTest() {
    assertEquals(1, resource.getResources().size());
    assertEquals(0, resourceWithUriParams.getResources().size());
  }

  @Test
  public void getParentUriTest() {
    assertEquals(StringUtils.EMPTY, resource.getParentUri());
    assertEquals(TEAMS_RESOURCE, resourceWithUriParams.getParentUri());
    resource.setParentUri(parentUri);
    assertEquals(parentUri, resource.getParentUri());
  }

  @Test
  public void getActionsTest() {
    assertEquals(2, resource.getActions().size());
    assertEquals(3, resourceWithUriParams.getActions().size());
    assertEquals(1, resourceWithBaseUriParam.getActions().size());
  }

  @Test
  public void getResolvedUriParametersTest() {
    assertEquals(0, resource.getResolvedUriParameters().size());
    assertEquals(1, resourceWithUriParams.getResolvedUriParameters().size());
    assertEquals(2, resourceWithBaseUriParam.getResolvedUriParameters().size());
  }

  @Test
  public void getBaseUriParametersTest() {
    assertEquals(0, resource.getBaseUriParameters().size());
    assertEquals(0, resourceWithUriParams.getBaseUriParameters().size());
    assertEquals(1, resourceWithBaseUriParam.getBaseUriParameters().size());
    resourceWithBaseUriParam.cleanBaseUriParameters();
    assertEquals(0, resourceWithBaseUriParam.getBaseUriParameters().size());
  }

  @Test
  public void getDisplayNameTest() {
    assertEquals("Teams", resource.getDisplayName());
  }

  @Test
  public void getRelativeUriTest() {
    assertEquals(TEAMS_RESOURCE, resource.getRelativeUri());
    assertEquals(TEAM_ID_RESOURCE, resourceWithUriParams.getRelativeUri());
    assertEquals(TEAMS_HISTORY_YEAR_RESOURCE, resourceWithBaseUriParam.getRelativeUri());
  }

  @Test
  public void toStringTest() {
    assertEquals(TEAMS_RESOURCE, resource.toString());
  }
}
