/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import org.junit.Test;
import org.mule.amf.impl.model.AMFImpl;
import org.mule.apikit.model.api.ApiReference;

import java.net.URISyntaxException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OasSpecificationTestCase {

  @Test
  public void apiWithCallbacksTest() throws URISyntaxException {
    AMFImpl api = getApiFromPath("openapi-specification/examples/v3.0/callback-example.yaml");
    assertTrue(api.includesCallbacks());
    api = getApiFromPath("openapi-specification/examples/v3.0/callback-example.json");
    assertTrue(api.includesCallbacks());
  }

  @Test
  public void apiWithLinksTest() throws URISyntaxException {
    AMFImpl api = getApiFromPath("openapi-specification/examples/v3.0/link-example.yaml");
    assertTrue(api.includesLinks());
    api = getApiFromPath("openapi-specification/examples/v3.0/link-example.json");
    assertTrue(api.includesLinks());
  }

  @Test
  public void apiWithCallbackAndLinksTest() throws URISyntaxException {
    AMFImpl api = getApiFromPath("openapi-specification/examples/v3.0/callback-and-link-example.yaml");
    assertTrue(api.includesCallbacks());
    assertTrue(api.includesLinks());
    api = getApiFromPath("openapi-specification/examples/v3.0/callback-and-link-example.json");
    assertTrue(api.includesCallbacks());
    assertTrue(api.includesLinks());
  }

  @Test
  public void apiWithoutCallbacksOrLinksTest() throws URISyntaxException {
    AMFImpl api = getApiFromPath("openapi-specification/examples/v3.0/petstore.yaml");
    assertFalse(api.includesCallbacks());
    assertFalse(api.includesLinks());
    api = getApiFromPath("openapi-specification/examples/v3.0/petstore.json");
    assertFalse(api.includesCallbacks());
    assertFalse(api.includesLinks());
  }

  private AMFImpl getApiFromPath(String apiPath) throws URISyntaxException {
    String apiLocation = OasSpecificationTestCase.class.getResource(apiPath).toURI().toString();
    ApiReference apiRef = ApiReference.create(apiLocation);
    return (AMFImpl) new AMFParser(apiRef).parse();
  }
}
