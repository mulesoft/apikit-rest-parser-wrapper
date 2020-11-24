/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.model.parameter.Parameter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Arrays.asList;

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

    return asList(new Object[][] {
        {ParserMode.AMF, parserService.parse(ramlApiRef, ParserMode.AMF).get()},
        {ParserMode.RAML, parserService.parse(ramlApiRef, ParserMode.RAML).get()}
    });
  }

  @Test
  public void testNumberUriParamsConstrains() {
    final Map<String, Resource> resources = api.getResources();
    Set<String> positiveForAll = new HashSet<>(Arrays.asList("00123", "0"));
    Set<String> negativeForAll = new HashSet<>(Arrays.asList("hola", "000.xz", "01253a"));

    Parameter longUriParam = resources.get("/{long}").getAction(GET_ACTION).getResolvedUriParameters().get("long");
    positiveAssert(longUriParam, positiveForAll, String.valueOf(Long.MAX_VALUE));
    negativeAssert(longUriParam, negativeForAll, Long.MAX_VALUE + "1");


    Parameter integerUriParam = resources.get("/{integer}").getAction(GET_ACTION).getResolvedUriParameters().get("integer");
    positiveAssert(integerUriParam, positiveForAll, String.valueOf(Integer.MAX_VALUE));
    negativeAssert(integerUriParam, negativeForAll, Long.MAX_VALUE + "1");


    Parameter floatUriParam = resources.get("/{float}").getAction(GET_ACTION).getResolvedUriParameters().get("float");
    positiveAssert(floatUriParam, positiveForAll, "00.123", "0.1", String.valueOf(Float.MAX_VALUE));
    negativeAssert(floatUriParam, negativeForAll);

    Parameter doubleUriParam = resources.get("/{double}").getAction(GET_ACTION).getResolvedUriParameters().get("double");
    positiveAssert(doubleUriParam, positiveForAll, "00.123", "0.1", String.valueOf(Double.MAX_VALUE));
    negativeAssert(doubleUriParam, negativeForAll);
  }

  private static void positiveAssert(Parameter parameter, Set<String> values, String... additionalValues) {
    assertParameter(Assert::assertTrue, parameter, values, additionalValues);
  }

  private static void negativeAssert(Parameter parameter, Set<String> values, String... additionalValues) {
    assertParameter(Assert::assertFalse, parameter, values, additionalValues);
  }

  private static void assertParameter(Consumer<Boolean> assertion, Parameter parameter, Set<String> values,
                                      String... additionalValues) {
    HashSet<String> valuesToAssert = new HashSet<>(asList(additionalValues));
    valuesToAssert.addAll(values);

    valuesToAssert.stream().forEach(value -> assertion.accept(parameter.validate(value)));
  }
}
