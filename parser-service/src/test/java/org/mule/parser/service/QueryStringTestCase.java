/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.QueryString;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.api.ApiReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mule.apikit.model.ActionType.GET;

@RunWith(Parameterized.class)
public class QueryStringTestCase {

  @Parameterized.Parameter
  public ParserMode parserMode;

  @Parameterized.Parameter(1)
  public ApiSpecification api;

  private QueryString locations;

  @Parameterized.Parameters(name = "{0}")
  public static Collection apiSpecifications() {
    ApiReference ramlApiRef = ApiReference.create("query-string/api.raml");
    ParserService parserService = new ParserService();

    return asList(new Object[][] {
        {ParserMode.AMF, parserService.parse(ramlApiRef, ParserMode.AMF).get()},
        {ParserMode.RAML, parserService.parse(ramlApiRef, ParserMode.RAML).get()}
    });
  }

  @Before
  public void setup() {
    Map<String, Resource> resources = api.getResources();
    Action testQueryStringAction = resources.get("/locations").getAction(GET.name());
    this.locations = testQueryStringAction.queryString();
  }

  @Test
  public void notValidAdditionalPropertiesTest() {
    Map<String, List<String>> queryStringMap = new HashMap<>();

    queryStringMap.put("lat", singletonList("123"));
    queryStringMap.put("long", singletonList("123"));
    assertTrue(locations.validate(queryStringMap));// assert required properties

    queryStringMap.put("start", singletonList("123"));
    queryStringMap.put("page-size", singletonList("123"));
    assertTrue(locations.validate(queryStringMap));// assert required + optional properties

    queryStringMap.put("someExtraParam", singletonList("something"));
    assertFalse(locations.validate(queryStringMap));// assert required + optional + not valid additional properties
  }

  @Test
  public void validAdditionalPropertiesTest() {
    Map<String, List<String>> queryStringMap = new HashMap<>();

    queryStringMap.put("location", singletonList("123"));
    assertTrue(locations.validate(queryStringMap));// assert required properties

    queryStringMap.put("start", singletonList("123"));
    assertTrue(locations.validate(queryStringMap));// assert required + optional properties

    queryStringMap.put("someExtraParam", singletonList("something"));
    assertTrue(locations.validate(queryStringMap));// assert required + optional + valid additional properties
  }
}
