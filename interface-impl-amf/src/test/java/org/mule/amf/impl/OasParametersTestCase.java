/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import org.junit.Test;
import org.mule.amf.impl.model.AMFImpl;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.model.parameter.Parameter;

import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class OasParametersTestCase {

  @Test
  public void testParametersNameResolution() throws URISyntaxException {
    String apiLocation = OasParametersTestCase.class.getResource("oas-parameters/oas30-api.json").toURI().toString();
    ApiReference apiRef = ApiReference.create(apiLocation);
    AMFImpl api = (AMFImpl) new AMFParser(apiRef).parse();
    // Products
    Resource resource = api.getResources().get("/products");
    Action getAction = resource.getAction("GET");
    Map<String, Parameter> headers = getAction.getHeaders();
    assertNotNull(headers.get("X-Client-Id"));
    assertNotNull(headers.get("X-Client-Secret"));
    assertNotNull(headers.get("X-Correlation-Id"));
    Map<String, Parameter> parameters = getAction.getQueryParameters();
    assertNotNull(parameters.get("page_index"));
    assertNotNull(parameters.get("page_size"));
    assertNotNull(parameters.get("search"));
    // Product
    resource = api.getResources().get("/products/{product}");
    getAction = resource.getAction("GET");
    headers = getAction.getHeaders();
    assertNotNull(headers.get("X-Client-Id"));
    assertNotNull(headers.get("X-Client-Secret"));
    assertNotNull(headers.get("X-Correlation-Id"));
    parameters = getAction.getResolvedUriParameters();
    assertNotNull(parameters.get("product"));
  }
}
