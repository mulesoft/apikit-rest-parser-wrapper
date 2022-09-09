/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.api.ApiReference;

import java.util.Collection;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(Parameterized.class)
public class ExampleParsingTestCase {

  @Parameterized.Parameter
  public ParserMode parserMode;

  @Parameterized.Parameter(1)
  public ApiSpecification api;

  @Parameterized.Parameters(name = "{0}")
  public static Collection apiSpecifications() {
    ApiReference ramlApiRef = ApiReference.create("apis/api-with-examples/examplesapi.raml");
    ParserService parserService = new ParserService();

    return asList(new Object[][] {
        {ParserMode.AMF, parserService.parse(ramlApiRef, ParserMode.AMF).get()},
        {ParserMode.RAML, parserService.parse(ramlApiRef, ParserMode.RAML).get()}
    });
  }

  /**
   * This test exemplifies and documents how each parser handles integer values that contains leading zeros.
   */
  @Test
  public void parseYamlIntWithLeadingZerosExample() {
    Map<String, String> examples = api.getResources().get("/organizations").getResources().get("/employees").getAction("GET")
        .getResponses().get("200").getExamples();
    assertFalse(examples.isEmpty());
    if (ParserMode.AMF.equals(parserMode)) {
      assertEquals("[{\"name\":\"Pedro\",\"lastname\":\"Garcia\",\"age\":42,\"role\":{\"name\":\"EmployeeSuccess\"}},{\"name\":\"Marta\",\"lastname\":\"Gonzales\",\"age\":36,\"role\":{\"name\":\"Manager\"}}]",
                   minifyJson(examples.get("application/json")));
    } else if (ParserMode.RAML.equals(parserMode)) {
      assertEquals("[{\"name\":\"Pedro\",\"lastname\":\"Garcia\",\"age\":042,\"role\":{\"name\":\"EmployeeSuccess\"}},{\"name\":\"Marta\",\"lastname\":\"Gonzales\",\"age\":036,\"role\":{\"name\":\"Manager\"}}]",
                   minifyJson(examples.get("application/json")));
    }
  }

  /**
   * This test exemplifies and documents how each parser handles float values that contains leading zeros.
   */
  @Test
  public void parseYamlFloatWithLeadingZerosExample() {
    Map<String, String> examples = api.getResources().get("/organizations").getResources().get("/locations").getResources()
        .get("/{coordinates}").getAction("GET").getResponses().get("200").getExamples();
    assertFalse(examples.isEmpty());
    if (ParserMode.AMF.equals(parserMode)) {
      assertEquals("{\"latitude\":\"02435.122\",\"longitude\":\"014.244\"}", minifyJson(examples.get("application/json")));
    } else if (ParserMode.RAML.equals(parserMode)) {
      assertEquals("{\"latitude\":02435.122,\"longitude\":014.244}", minifyJson(examples.get("application/json")));
    }
  }

  private String minifyJson(String jsonValue) {
    return jsonValue.trim().replaceAll("[\\t\\n\\r\\s]+", StringUtils.EMPTY);
  }
}
