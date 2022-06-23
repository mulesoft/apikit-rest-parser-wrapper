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
import org.mule.apikit.model.parameter.Parameter;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mule.apikit.model.ActionType.GET;

@RunWith(Parameterized.class)
public class QueryConsistencyTestCase {

  private static final String STRING_ITEM_PARAM = "stringItemParam";
  private static final String NUMERIC_ITEM_PARAM = "numericItemParam";
  private static final String INTEGER_ITEM_PARAM = "integerItemParam";
  private static final String BOOLEAN_ITEM_PARAM = "booleanItemParam";
  private static final String DATETIME_ITEM_PARAM = "datetimeItemParam";
  private static final String UNION_ITEM_PARAM = "unionItemParam";
  private static final String NON_STRING_UNION_ITEM_PARAM = "nonStringUnionItemParam";
  private static final String OBJECT_ITEM_PARAM = "objectItemParam";
  private static final String STRING_ITEM_PARAMS = "stringItemParams";
  private static final String NUMERIC_ITEM_PARAMS = "numericItemParams";
  private static final String INTEGER_ITEM_PARAMS = "integerItemParams";
  private static final String BOOLEAN_ITEM_PARAMS = "booleanItemParams";
  private static final String DATETIME_ITEM_PARAMS = "datetimeItemParams";
  private static final String UNION_ITEM_PARAMS = "unionItemParams";
  private static final String NON_STRING_UNION_ITEM_PARAMS = "nonStringUnionItemParams";
  private static final String OBJECT_ITEM_PARAMS = "objectItemParams";
  private static final String UNION_OF_ARRAYS = "unionOfArraysParams";
  private static final String NON_NULLABLE_UNION_OF_ARRAYS = "nonNullableUnionOfArraysParams";

  private Map<String, Parameter> queryParameters;
  private QueryString queryString;

  @Parameterized.Parameter
  public ParserMode parserMode;

  @Parameterized.Parameter(1)
  public ApiSpecification api;

  @Parameterized.Parameters(name = "{0}")
  public static Collection apiSpecifications() {
    ApiReference ramlApiRef = ApiReference.create("query-validation-consistency/api.raml");
    ParserService parserService = new ParserService();

    return asList(new Object[][] {
        {ParserMode.AMF, parserService.parse(ramlApiRef, ParserMode.AMF).get()},
        {ParserMode.RAML, parserService.parse(ramlApiRef, ParserMode.RAML).get()}
    });
  }

  @Before
  public void setup() throws URISyntaxException {
    Map<String, Resource> resources = api.getResources();
    this.queryParameters = resources.get("/testQueryParams").getAction(GET.name()).getQueryParameters();
    Action testQueryStringAction = resources.get("/testQueryString").getAction(GET.name());
    this.queryString = testQueryStringAction.queryString();
  }

  @Test
  public void testStringValidation() {
    validateConsistency(true, STRING_ITEM_PARAM, "test");
    validateConsistency(true, STRING_ITEM_PARAM, "12345");
    validateConsistency(true, STRING_ITEM_PARAM, "*test");
    validateConsistency(false, STRING_ITEM_PARAM, "exceedMaxLength");
  }


  @Test
  public void testNumberValidation() {
    validateConsistency(true, NUMERIC_ITEM_PARAM, "12345");
    validateConsistency(true, NUMERIC_ITEM_PARAM, "123.34");
    validateConsistency(false, NUMERIC_ITEM_PARAM, "ABC");
  }

  @Test
  public void testIntegerValidation() {
    validateConsistency(true, INTEGER_ITEM_PARAM, "12345");
    validateConsistency(false, INTEGER_ITEM_PARAM, "12.55");
    validateConsistency(false, INTEGER_ITEM_PARAM, "ABC");
  }

  @Test
  public void testBooleanValidation() {
    validateConsistency(true, BOOLEAN_ITEM_PARAM, "true");
    validateConsistency(true, BOOLEAN_ITEM_PARAM, "False");
    validateConsistency(false, BOOLEAN_ITEM_PARAM, "ABC");
  }

  @Test
  public void testDatetimeValidation() {
    validateConsistency(true, DATETIME_ITEM_PARAM, "2016-02-28T16:41:41.090Z");
    validateConsistency(false, DATETIME_ITEM_PARAM, "12016-02-28T16:41:41.090Z");
  }

  @Test
  public void testUnionValidation() {
    validateConsistency(true, UNION_ITEM_PARAM, "ABC");
    validateConsistency(true, UNION_ITEM_PARAM, "123");
    validateConsistency(true, UNION_ITEM_PARAM, "{\"someField\": \"someValue\"}");
    validateConsistency(true, UNION_ITEM_PARAM, "\n- \"firstValue\"\n- \"secondValue\"\n");
  }

  @Test
  public void testNonStringUnionValidation() {
    validateConsistency(true, NON_STRING_UNION_ITEM_PARAM, "true");
    validateConsistency(true, NON_STRING_UNION_ITEM_PARAM, "False");
    validateConsistency(true, NON_STRING_UNION_ITEM_PARAM, "123");
    validateConsistency(true, NON_STRING_UNION_ITEM_PARAM, "123.456");
    validateConsistency(false, NON_STRING_UNION_ITEM_PARAM, "ABC");
    validateConsistency(false, NON_STRING_UNION_ITEM_PARAM, "\"123\"");
  }

  @Test
  public void testObjectValidation() {
    validateConsistency(true, OBJECT_ITEM_PARAM,
                        "{\"stringProp\":\"test\",\"numberProp\":0.10,\"integerProp\":3,\"booleanProp\":false,\"datetimeProp\":\"2016-02-28T16:41:41.090Z\",\"stringProps\":[\"A\",\"B\"],\"numberProps\":[0,1.26],\"integerProps\": [0, 1, 2],\"booleanProps\":[false,true,true],\"datetimeProps\":[\"2016-02-28T16:41:41.090Z\",\"2016-02-28T16:41:41.090Z\"]}");
    validateConsistency(false, OBJECT_ITEM_PARAM, "{\"integerProp\":\"ABC\"}");
  }

  @Test
  public void testStringArrayValidation() {
    validateArrayConsistency(true, STRING_ITEM_PARAMS, asList("ABC", "12345", "*test", "AB", "BC", "CD"));
    validateArrayConsistency(false, STRING_ITEM_PARAMS, asList("exceedsMaxLength"));
    validateArrayConsistency(false, STRING_ITEM_PARAMS, asList("A"));
  }

  @Test
  public void testNumberArrayValidation() {
    validateArrayConsistency(true, NUMERIC_ITEM_PARAMS, asList("123", "45.67"));
    validateArrayConsistency(false, NUMERIC_ITEM_PARAMS, asList("123", "ABC"));
  }

  @Test
  public void testIntegerArrayValidation() {
    validateArrayConsistency(true, INTEGER_ITEM_PARAMS, asList("123", "4567"));
    validateArrayConsistency(false, INTEGER_ITEM_PARAMS, asList("123", "ABC"));
    validateArrayConsistency(false, INTEGER_ITEM_PARAMS, asList("45.67"));
  }

  @Test
  public void testBooleanArrayValidation() {
    validateArrayConsistency(true, BOOLEAN_ITEM_PARAMS, asList("true", "False"));
    validateArrayConsistency(false, BOOLEAN_ITEM_PARAMS, asList("ABC"));
  }

  @Test
  public void testDatetimeArrayValidation() {
    validateArrayConsistency(true, DATETIME_ITEM_PARAMS, asList("2016-02-28T16:41:41.090Z", "2016-01-28T16:41:41.090Z"));
    validateArrayConsistency(false, DATETIME_ITEM_PARAMS, asList("12016-02-28T16:41:41.090Z"));
  }

  @Test
  public void testUnionItemArrayValidation() {
    validateArrayConsistency(true, UNION_ITEM_PARAMS, asList("ABC", "123", "\"ABC123\"", "*something*", "\\else\\"));
  }

  @Test
  public void testNonStringUnionItemArrayValidation() {
    validateArrayConsistency(true, NON_STRING_UNION_ITEM_PARAMS, asList("123", "45.67", "true", "FALSE"));
    validateArrayConsistency(false, NON_STRING_UNION_ITEM_PARAMS, asList("123", "ABC"));
  }

  @Test
  public void testObjectArrayValidation() {
    validateArrayConsistency(true, OBJECT_ITEM_PARAMS,
                             asList("{\"stringProp\":\"test\",\"numberProp\":0.10,\"integerProp\":3,\"booleanProp\":false,\"datetimeProp\":\"2016-02-28T16:41:41.090Z\",\"stringProps\":[\"A\",\"B\"],\"numberProps\":[0,1.26],\"integerProps\": [0, 1, 2],\"booleanProps\":[false,true,true],\"datetimeProps\":[\"2016-02-28T16:41:41.090Z\",\"2016-02-28T16:41:41.090Z\"]}",
                                    "{\"stringProp\":\"test\",\"numberProp\":0.10,\"integerProp\":3,\"booleanProp\":false,\"datetimeProp\":\"2016-02-28T16:41:41.090Z\",\"stringProps\":[\"A\",\"B\"],\"numberProps\":[0,1.26],\"integerProps\": [0, 1, 2],\"booleanProps\":[false,true,true],\"datetimeProps\":[\"2016-02-28T16:41:41.090Z\",\"2016-02-28T16:41:41.090Z\"]}"));
    validateArrayConsistency(false, OBJECT_ITEM_PARAMS, asList("{\"integerProp\":ABC}", "{\"integerProp\":3}"));
  }


  @Test
  public void testUnionOfArraysValidation() {
    if (parserMode.equals(ParserMode.AMF)) {// RAML Parser query string union of arrays validation is not supported
      validateArrayConsistency(true, UNION_OF_ARRAYS, asList("123", "456"));
      validateArrayConsistency(true, UNION_OF_ARRAYS, asList("false", "true"));
      validateArrayConsistency(false, UNION_OF_ARRAYS, asList("123", "true"));
      validateArrayConsistency(true, UNION_OF_ARRAYS, null);
      validateArrayConsistency(true, UNION_OF_ARRAYS, singletonList(null));
      validateArrayConsistency(true, UNION_OF_ARRAYS, singletonList("null"));
    }
  }

  @Test
  public void testNonNullableUnionOfArraysValidation() {
    if (parserMode.equals(ParserMode.AMF)) {// RAML Parser query string union of arrays validation is not supported
      validateArrayConsistency(true, NON_NULLABLE_UNION_OF_ARRAYS, asList("123", "456"));
      validateArrayConsistency(true, NON_NULLABLE_UNION_OF_ARRAYS, asList("false", "true"));
      validateArrayConsistency(false, NON_NULLABLE_UNION_OF_ARRAYS, asList("123", "true"));
      validateArrayConsistency(false, NON_NULLABLE_UNION_OF_ARRAYS, null);
      validateArrayConsistency(false, NON_NULLABLE_UNION_OF_ARRAYS, singletonList(null));
      validateArrayConsistency(false, NON_NULLABLE_UNION_OF_ARRAYS, singletonList("null"));
    }
  }

  public void validateConsistency(boolean expectedResult, String paramName, String value) {
    assertQueryParamValue(expectedResult, paramName, value);
    assertQueryStringValue(expectedResult, singletonMap(paramName, asList(value)));
  }

  public void validateArrayConsistency(boolean expectedResult, String paramName, List<String> values) {
    assertQueryParamArrayValue(expectedResult, paramName, values);
    assertQueryStringValue(expectedResult, singletonMap(paramName, values));
  }

  private void assertQueryParamValue(boolean expectedResult, String queryParamName, String value) {
    Parameter queryParam = queryParameters.get(queryParamName);
    assertExpectation(expectedResult, queryParam.validate(value));
  }

  private void assertQueryParamArrayValue(boolean expectedResult, String queryParamName, List<String> values) {
    Parameter queryParam = queryParameters.get(queryParamName);
    assertExpectation(expectedResult, queryParam.validateArray(values));
  }

  private void assertQueryStringValue(boolean expectedResult, Map<String, Collection<?>> values) {
    assertExpectation(expectedResult, queryString.validate(values));
  }

  public void assertExpectation(boolean expectedResult, boolean validate) {
    if (expectedResult) {
      assertTrue(validate);
    } else {
      assertFalse(validate);
    }
  }

}
