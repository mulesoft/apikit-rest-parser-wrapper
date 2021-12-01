/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v10.model;

import org.apache.commons.collections.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.implv2.ParserWrapperV2;
import org.mule.apikit.model.Response;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ResponseImplTest {

  private static final String RESOURCE = "/leagues";
  private static final String ACTION = "GET";
  private Response response;

  @Before
  public void setUp() throws Exception {
    String apiLocation = this.getClass().getResource("/apis/10-leagues/api.raml").toURI().toString();
    RamlImpl10V2 parser = (RamlImpl10V2) new ParserWrapperV2(apiLocation, Collections.emptyList()).parse();
    ActionImpl action = (ActionImpl) parser.getResources().get(RESOURCE).getAction(ACTION);
    response = action.getResponses().get("200");
  }

  @Test
  public void getBodyTest() {
    assertEquals(2, response.getBody().size());
  }

  @Test
  public void hasBodyTest() {
    assertTrue(response.hasBody());
  }

  @Test
  public void getHeadersTest() {
    assertEquals(1, response.getHeaders().size());// TODO: APIKIT-2509 check difference with amf
  }

  @Test(expected = UnsupportedOperationException.class) // TODO: APIKIT-2509 check difference with amf
  public void getInstanceTest() {
    response.getInstance();
  }

  @Test
  public void getExamplesTest() {
    assertEquals(2, response.getExamples().size());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void setBodyTest() {
    response.setBody(MapUtils.EMPTY_MAP);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void setHeadersTest() {
    response.setHeaders(MapUtils.EMPTY_MAP);
  }
}
