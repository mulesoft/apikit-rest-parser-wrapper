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
import java.util.Map;

import static java.util.Arrays.asList;
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
  private static final String OBJECT_ITEM_PARAM = "objectItemParam";
  private static final String STRING_ITEM_PARAMS = "stringItemParams";
  private static final String NUMERIC_ITEM_PARAMS = "numericItemParams";
  private static final String INTEGER_ITEM_PARAMS = "integerItemParams";
  private static final String BOOLEAN_ITEM_PARAMS = "booleanItemParams";
  private static final String DATETIME_ITEM_PARAMS = "datetimeItemParams";
  private static final String OBJECT_ITEM_PARAMS = "objectItemParams";

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
    assertValue(true, STRING_ITEM_PARAM, "test", true);
    assertValue(true, STRING_ITEM_PARAM, "12345", true);
    assertValue(true, STRING_ITEM_PARAM, "*test", true);
    assertValue(false, STRING_ITEM_PARAM, "exceedMaxLength", true);
  }


  @Test
  public void testNumberValidation() {
    assertValue(true, NUMERIC_ITEM_PARAM, "12345", false);
    assertValue(true, NUMERIC_ITEM_PARAM, "123.34", false);
    assertValue(false, NUMERIC_ITEM_PARAM, "ABC", false);
  }

  @Test
  public void testIntegerValidation() {
    assertValue(true, INTEGER_ITEM_PARAM, "12345", false);
    assertValue(false, INTEGER_ITEM_PARAM, "12.55", false);
    assertValue(false, INTEGER_ITEM_PARAM, "ABC", false);
  }

  @Test
  public void testBooleanValidation() {
    assertValue(true, BOOLEAN_ITEM_PARAM, "true", false);
    assertValue(true, BOOLEAN_ITEM_PARAM, "False", false);
    assertValue(false, BOOLEAN_ITEM_PARAM, "ABC", false);
  }

  @Test
  public void testDatetimeValidation() {
    assertValue(true, DATETIME_ITEM_PARAM, "2016-02-28T16:41:41.090Z", false);
    assertValue(false, DATETIME_ITEM_PARAM, "12016-02-28T16:41:41.090Z", false);
  }

  @Test
  public void testObjectValidation() {
    assertValue(true, OBJECT_ITEM_PARAM,
                "{\"stringProp\":\"test\",\"numberProp\":0.10,\"integerProp\":3,\"booleanProp\":false,\"datetimeProp\":\"2016-02-28T16:41:41.090Z\",\"stringProps\":[\"A\",\"B\"],\"numberProps\":[0,1.26],\"integerProps\": [0, 1, 2],\"booleanProps\":[false,true,true],\"datetimeProps\":[\"2016-02-28T16:41:41.090Z\",\"2016-02-28T16:41:41.090Z\"]}",
                false);
    assertValue(false, OBJECT_ITEM_PARAM, "{\"integerProp\":\"ABC\"}", false);
  }

  @Test
  public void testStringArrayValidation() {
    assertValue(true, STRING_ITEM_PARAMS, "\n- \"ABC\"\n- \"12345\"\n- \"*test\"\n- \"A\\\"B\\\"C\"\n", false);
    assertValue(false, STRING_ITEM_PARAMS, "\n- \"exceedsMaxLength\"\n", false);
  }

  @Test
  public void testNumberArrayValidation() {
    assertValue(true, NUMERIC_ITEM_PARAMS, "\n- 123\n- 45.67", false);
    assertValue(false, NUMERIC_ITEM_PARAMS, "\n- 123\n- ABC", false);
  }

  @Test
  public void testIntegerArrayValidation() {
    assertValue(true, INTEGER_ITEM_PARAMS, "\n- 123\n- 4567", false);
    assertValue(false, INTEGER_ITEM_PARAMS, "\n- ABC\n", false);
  }

  @Test
  public void testBooleanArrayValidation() {
    assertValue(true, BOOLEAN_ITEM_PARAMS, "\n- true\n- False", false);
    assertValue(false, BOOLEAN_ITEM_PARAMS, "\n- ABC", false);
  }

  @Test
  public void testDatetimeArrayValidation() {
    assertValue(true, DATETIME_ITEM_PARAMS, "\n- \"2016-02-28T16:41:41.090Z\"\n- \"2016-01-28T16:41:41.090Z\"", false);
    assertValue(false, DATETIME_ITEM_PARAMS, "\n- \"12016-02-28T16:41:41.090Z\"", false);
  }

  @Test
  public void testObjectArrayValidation() {
    assertValue(true, OBJECT_ITEM_PARAMS,
                "\n- {\"stringProp\":\"test\",\"numberProp\":0.10,\"integerProp\":3,\"booleanProp\":false,\"datetimeProp\":\"2016-02-28T16:41:41.090Z\",\"stringProps\":[\"A\",\"B\"],\"numberProps\":[0,1.26],\"integerProps\": [0, 1, 2],\"booleanProps\":[false,true,true],\"datetimeProps\":[\"2016-02-28T16:41:41.090Z\",\"2016-02-28T16:41:41.090Z\"]}\n"
                    + "- {\"stringProp\":\"test\",\"numberProp\":0.10,\"integerProp\":3,\"booleanProp\":false,\"datetimeProp\":\"2016-02-28T16:41:41.090Z\",\"stringProps\":[\"A\",\"B\"],\"numberProps\":[0,1.26],\"integerProps\": [0, 1, 2],\"booleanProps\":[false,true,true],\"datetimeProps\":[\"2016-02-28T16:41:41.090Z\",\"2016-02-28T16:41:41.090Z\"]}",
                false);
    assertValue(false, OBJECT_ITEM_PARAMS, "\n- {\"integerProp\":ABC}\n- {\"integerProp\":3}", false);
  }

  private void assertValue(boolean expectedResult, String queryParamName, String value, boolean yamlNeedsQuotes) {
    Parameter queryParam = queryParameters.get(queryParamName);
    boolean validate = queryParam.validate(value);
    if (expectedResult) {
      assertTrue(validate);
    } else {
      assertFalse(validate);
    }
    String yamlValue = getYamlKeyValue(queryParamName, value, yamlNeedsQuotes);
    validate = queryString.validate(yamlValue);
    if (expectedResult) {
      assertTrue(validate);
    } else {
      assertFalse(validate);
    }
  }

  private String getYamlKeyValue(String paramName, String value, boolean needsQuotes) {
    return paramName + ": " + (needsQuotes ? "\"" + value + "\"" : value);
  }
}
