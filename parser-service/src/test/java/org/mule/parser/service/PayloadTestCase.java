/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.validation.ApiValidationResult;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;

public class PayloadTestCase {

  private static ApiSpecification api;

  @BeforeClass
  public static void setup() {
    ApiReference ramlApiRef = ApiReference.create("api-10.raml");
    ParserService parserService = new ParserService();
    api = parserService.parse(ramlApiRef, ParserMode.AMF).get();
  }

  @Test
  public void invalidTextAtTheRightOfObjectPayload() {
    final Map<String, Resource> resources = api.getResources();
    List<ApiValidationResult> results = resources.get("/payload").getAction("POST").getBody().get("application/json")
        .validate("{\"a\": \"aaaa\", \"b\": \"bbb\"}asdfgh");
    assertFalse(results.isEmpty());
  }

  @Test
  public void invalidTextAtTheRightOfArrayPayload() {
    final Map<String, Resource> resources = api.getResources();
    List<ApiValidationResult> results =
        resources.get("/payload").getAction("PUT").getBody().get("application/json").validate("[\"aaaa\",\"bbb\"]asdfgh");
    assertFalse(results.isEmpty());
  }
}
