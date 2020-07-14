/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import org.junit.Before;
import org.junit.Test;
import org.mule.amf.impl.AMFParser;
import org.mule.apikit.implv1.model.MimeTypeImpl;
import org.mule.apikit.implv1.model.parameter.ParameterImpl;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.model.parameter.Parameter;
import org.raml.model.parameter.Header;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ResponseImplTest {

  private static final String RESOURCE = "/leagues";
  private static final String ACTION = "GET";
  private static final String APPLICATION_XML = "application/xml";
  private static final String CONTENT_TYPE = "content-type";
  private Response response;

  @Before
  public void setUp() throws Exception {
    String apiLocation = this.getClass().getResource("../10-leagues/api.raml").toURI().toString();
    ApiReference apiRef = ApiReference.create(apiLocation);
    ResourceImpl resource = (ResourceImpl) new AMFParser(apiRef, true).parse().getResource(RESOURCE);
    response = resource.getAction(ACTION).getResponses().get("200");
  }

  @Test
  public void getBodyTest() {
    assertEquals(2, response.getBody().size());
  }

  @Test
  public void setBodyTest() {
    Map<String, MimeType> body = new HashMap<>();
    body.put(APPLICATION_XML, new MimeTypeImpl(new org.raml.model.MimeType()));
    response.setBody(body);
    // Assert that it does nothing
    assertEquals(2, response.getBody().size());
  }

  @Test
  public void hasBodyTest() {
    assertTrue(response.hasBody());
  }

  @Test
  public void getHeadersTest() {
    assertNull(response.getHeaders());
  }

  @Test
  public void setHeadersTest() {
    Map<String, Parameter> headers = new HashMap<>();
    headers.put(CONTENT_TYPE, new ParameterImpl(new Header()));
    response.setHeaders(headers);
    // Assert that it does nothing
    assertNull(response.getHeaders());
  }

  @Test
  public void getInstanceTest() {
    assertNull(response.getInstance());
  }

  @Test
  public void getExamplesTest() {
    assertEquals(2, response.getExamples().size());
  }
}
