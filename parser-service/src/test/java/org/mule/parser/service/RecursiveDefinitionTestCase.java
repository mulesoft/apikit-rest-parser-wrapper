/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.internal.ParserService;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class RecursiveDefinitionTestCase {

  private static final String RECURSIVE_RESOURCE = "/recursive";
  private static final String POST_ACTION = "POST";
  private static final String APPLICATION_JSON = "application/json";

  @Parameterized.Parameter
  public ApiVendor apiVendor;

  @Parameterized.Parameter(1)
  public ParserMode parserMode;

  @Parameterized.Parameter(2)
  public ApiSpecification api;

  @Parameterized.Parameters(name = "{0} - {1}")
  public static Collection apiSpecifications() {
    ApiReference ramlApiRef = ApiReference.create("recursive-definition/recursive-definition.raml");
    ApiReference oas30apiRef = ApiReference.create("recursive-definition/recursive-definition.json");
    ParserService parserService = new ParserService();
    return asList(new Object[][] {
        {ApiVendor.RAML, ParserMode.AMF, parserService.parse(ramlApiRef, ParserMode.AMF).get()},
        {ApiVendor.RAML, ParserMode.RAML, parserService.parse(ramlApiRef, ParserMode.RAML).get()},
        {ApiVendor.OAS_30, ParserMode.AMF, parserService.parse(oas30apiRef, ParserMode.AMF).get()}
    });
  }

  @Test
  public void validJsonWithRecursiveDefinition() {
    MimeType mimeType = api.getResources().get(RECURSIVE_RESOURCE).getAction(POST_ACTION).getBody().get(APPLICATION_JSON);
    assertTrue(mimeType
        .validate("{\"value\": \"root\",\"left\": {\"value\": \"child1\"},\"right\": {\"value\": \"child2\",\"left\": {\"value\": \"child21\"},\"right\": {\"value\": \"child22\"}}}")
        .isEmpty());
    assertFalse(mimeType
        .validate("{\"value\": \"root\",\"left\": {\"value\": \"child1\"},\"right\": {\"value2\": \"child2\",\"left\": {\"value\": \"child21\"},\"right\": {\"value\": \"child22\"}}}")
        .isEmpty());
  }


}
