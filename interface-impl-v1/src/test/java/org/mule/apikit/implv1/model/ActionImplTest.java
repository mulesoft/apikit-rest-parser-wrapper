/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.apikit.implv1.model;

import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.common.LazyValue;
import org.mule.apikit.implv1.ParserWrapperV1;
import org.mule.apikit.implv1.model.parameter.ParameterImpl;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.parameter.Parameter;
import org.raml.model.Response;
import org.raml.model.parameter.Header;
import org.raml.model.parameter.QueryParameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ActionImplTest {

  private static final String CONTENT_TYPE = "Content-type";
  private static final String GET_ACTION = "GET";
  private static final String PUT_ACTION = "PUT";

  private ActionImpl positionsAction;
  private ActionImpl teamIdAction;
  private ActionImpl badgeAction;
  private ActionImpl teamsAction;
  private ActionImpl historyAction;

  @Before
  public void setUp() throws Exception {
    String apiLocation = this.getClass().getResource("/apis/08-leagues/api.raml").toURI().toString();
    RamlImplV1 parser = (RamlImplV1) new ParserWrapperV1(apiLocation, new LazyValue<>(Collections::emptyList)).parse();
    positionsAction = (ActionImpl) parser.getResource("/positions").getAction(GET_ACTION);
    Resource teamsResource = parser.getResource("/teams");
    teamIdAction = (ActionImpl) teamsResource.getResources().get("/{teamId}").getAction(PUT_ACTION);
    teamsAction = (ActionImpl) teamsResource.getAction(GET_ACTION);
    badgeAction = (ActionImpl) parser.getResource("/badge").getAction(PUT_ACTION);
    historyAction = (ActionImpl) parser.getResource("/history/{version}").getResources().get("/{year}").getAction(GET_ACTION);
  }

  @Test
  public void getTypeTest() {
    assertEquals(GET_ACTION, positionsAction.getType().name());
  }

  @Test
  public void getResourceTest() {
    assertEquals("Position Table", positionsAction.getResource().getDisplayName());
  }

  @Test
  public void getBodyTest() {
    assertNull(positionsAction.getBody());
    Map<String, MimeType> body = new HashMap<>();
    body.put("body", new MimeTypeImpl(new org.raml.model.MimeType()));
    positionsAction.setBody(body);
    assertEquals(1, positionsAction.getBody().size());
    assertEquals(1, teamIdAction.getBody().size());
  }

  @Test
  public void getBaseUriParametersTest() {
    assertEquals(0, positionsAction.getBaseUriParameters().size());
    assertEquals(0, teamsAction.getBaseUriParameters().size());
    assertEquals(1, teamIdAction.getBaseUriParameters().size());
    assertEquals(1, badgeAction.getBaseUriParameters().size());
    badgeAction.cleanBaseUriParameters();
    assertEquals(0, badgeAction.getBaseUriParameters().size());
  }

  @Test
  public void getResolvedUriParametersTest() {
    assertEquals(0, positionsAction.getResolvedUriParameters().size());
    assertEquals(0, teamsAction.getBaseUriParameters().size());
    assertEquals(2, teamIdAction.getResolvedUriParameters().size());
    assertEquals(1, badgeAction.getResolvedUriParameters().size());
    assertEquals(2, historyAction.getResolvedUriParameters().size());
  }

  @Test
  public void getQueryParametersTest() {
    assertEquals(0, positionsAction.getQueryParameters().size());
    Map<String, Parameter> queryParams = new HashMap<>();
    queryParams.put("page", new ParameterImpl(new QueryParameter()));
    positionsAction.setQueryParameters(queryParams);
    assertEquals(1, positionsAction.getQueryParameters().size());
    assertEquals(0, teamIdAction.getQueryParameters().size());
    assertEquals(1, teamsAction.getQueryParameters().size());
  }

  @Test
  public void hasBodyTest() {
    assertFalse(positionsAction.hasBody());
    assertTrue(teamIdAction.hasBody());
  }

  @Test
  public void getResponsesTest() {
    assertEquals(1, positionsAction.getResponses().size());
    positionsAction.addResponse("201", new ResponseImpl(new Response()));
    assertEquals(2, positionsAction.getResponses().size());
    assertEquals(2, teamIdAction.getResponses().size());
    assertEquals(1, teamsAction.getResponses().size());
  }

  @Test
  public void queryStringTest() {
    assertNull(teamsAction.queryString());
  }

  @Test
  public void getHeadersTest() {
    assertEquals(0, positionsAction.getHeaders().size());
    Map<String, Parameter> headers = new HashMap<>();
    headers.put(CONTENT_TYPE, new ParameterImpl(new Header()));
    positionsAction.setHeaders(headers);
    assertEquals(1, positionsAction.getHeaders().size());
    assertEquals(1, badgeAction.getHeaders().size());
  }

  @Test
  public void getSecuredByTest() {
    assertEquals(0, positionsAction.getSecuredBy().size());
    positionsAction.addSecurityReference("customHeader");
    assertEquals(1, positionsAction.getSecuredBy().size());
    assertEquals(2, badgeAction.getSecuredBy().size());
  }

  @Test
  public void getIsTest() {
    assertEquals(0, positionsAction.getIs().size());
    positionsAction.addIs("secured");
    assertEquals(1, positionsAction.getIs().size());
    assertEquals(1, badgeAction.getIs().size());
  }

}
