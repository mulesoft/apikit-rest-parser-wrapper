/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.validation.ApiValidationResult;
import org.mule.parser.service.result.ParseResult;

import static org.junit.Assert.assertTrue;

public class ApiSyncTestCase {

  @Test
  public void fundamentalsTrainingTestCase() {
    assertTrue(getResult("resource::68ef9520-24e9-4cf2-b2f5-620025690913:training-american-flights-api:2.0.1:raml:zip:american-flights-api.raml")
        .success());
  }

  @Test
  public void resourceNameWithSpacesTestCase() {
    assertTrue(getResult("resource::c8cdb7d6-e052-449a-97f3-049f032ad03a:american-flights-api:1.0.5:raml:zip:american flights api.raml")
        .success());
  }


  @Test
  public void oasApiToRamlFragmentTestCase() {
    assertTrue(getResult("resource::820a4865-0c0a-4885-b029-25d6f5b31c9b:oas-api-test:1.0.0:oas:zip:oas-api-test.json")
        .success());
  }

  @Test
  public void parseXsdWithInclude() {
    ParseResult parserResult =
        getResult("resource::820a4865-0c0a-4885-b029-25d6f5b31c9b:xsd-with-include:1.0.0:raml:zip:api.raml");

    ApiSpecification specification = parserResult.get();

    Map<String, Resource> resources = specification.getResources();
    List<ApiValidationResult> results = resources
        .get("/test")
        .getAction("PUT")
        .getBody()
        .get("application/xml")
        .validate("<message xmlns=\"http://www.example.org/simple\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.example.org/simple schema.xsd\" item=\"content\"/>");
    assertTrue(results.isEmpty());
  }

  private ParseResult getResult(String location) {
    ApiReference apiReference = ApiReference.create(location, new ApiSyncResourceLoader());
    return new ParserService().parse(apiReference);
  }
}
