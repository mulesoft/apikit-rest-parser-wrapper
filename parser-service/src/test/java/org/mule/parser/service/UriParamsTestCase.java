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
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.model.parameter.Parameter;

import java.util.Collection;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class UriParamsTestCase {

  public static final String GET_ACTION = "GET";

  @Parameterized.Parameter
  public ParserMode parserMode;

  @Parameterized.Parameter(1)
  public ApiSpecification api;

  @Parameterized.Parameters(name = "{0}")
  public static Collection apiSpecifications() {
    ApiReference ramlApiRef = ApiReference.create("uri-params/api.raml");
    ParserService parserService = new ParserService();

    return asList(new Object[][]{
        {ParserMode.AMF, parserService.parse(ramlApiRef, ParserMode.AMF).get()},
        {ParserMode.RAML, parserService.parse(ramlApiRef, ParserMode.RAML).get()}
    });
  }

  @Test
  public void testNumberUriParamsConstrains() {
    final Map<String, Resource> resources = api.getResources();
    Parameter longUriParam = resources.get("/{long}").getAction(GET_ACTION).getResolvedUriParameters().get("long");
    assertTrue(longUriParam.validate("00123"));
    assertTrue(longUriParam.validate("0"));
    assertTrue(longUriParam.validate(String.valueOf(Long.MAX_VALUE)));

    assertFalse(longUriParam.validate("hola"));
    assertFalse(longUriParam.validate("000.xz"));
    assertFalse(longUriParam.validate("01253a"));
    assertFalse(longUriParam.validate(Long.MAX_VALUE + "1"));
    //assertFalse(longUriParam.validate("0.1")); TODO: uncomment when APIMF-2241 gets done

    Parameter integerUriParam = resources.get("/{integer}").getAction(GET_ACTION).getResolvedUriParameters().get("integer");
    assertTrue(integerUriParam.validate(String.valueOf(Integer.MAX_VALUE)));
    assertTrue(integerUriParam.validate("0"));
    assertTrue(integerUriParam.validate("00123"));

    assertFalse(integerUriParam.validate("hola"));
    assertFalse(integerUriParam.validate("000.xz"));
    assertFalse(integerUriParam.validate("01253a"));
    assertFalse(integerUriParam.validate(Long.MAX_VALUE + "1"));

    Parameter floatUriParam = resources.get("/{float}").getAction(GET_ACTION).getResolvedUriParameters().get("float");
    assertTrue(floatUriParam.validate("0"));
    assertTrue(floatUriParam.validate("00123"));
    assertTrue(floatUriParam.validate("00.123"));
    assertTrue(floatUriParam.validate(String.valueOf(Float.MAX_VALUE)));

    assertFalse(floatUriParam.validate("hola"));
    assertFalse(floatUriParam.validate("000.xz"));
    assertFalse(floatUriParam.validate("01253a"));

    Parameter doubleUriParam = resources.get("/{double}").getAction(GET_ACTION).getResolvedUriParameters().get("double");
    assertTrue(doubleUriParam.validate("00123"));
    assertTrue(doubleUriParam.validate("00.123"));
    assertTrue(doubleUriParam.validate("0"));
    assertTrue(doubleUriParam.validate(String.valueOf(Double.MAX_VALUE)));

    assertFalse(doubleUriParam.validate("hola"));
    assertFalse(doubleUriParam.validate("000.xz"));
    assertFalse(doubleUriParam.validate("01253a"));
  }

}
