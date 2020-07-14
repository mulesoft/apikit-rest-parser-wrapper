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
import org.mule.apikit.model.parameter.Parameter;
import org.mule.apikit.validation.ApiValidationResult;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class UnionTypesTestCase {

  private static final String BODY_UNION_RESOURCE = "/bodyunion";
  private static final String QUERY_PARAM_UNION_RESOURCE = "/queryparamunion";
  private static final String URI_PARAM_UNION_RESOURCE = "/uriparamunion/{dateortimestamp}";
  private static final String GET_ACTION = "GET";
  private static final String POST_ACTION = "POST";
  private static final String APPLICATION_JSON = "application/json";
  private static final String PARAM_VALUE = "dateortimestamp";

  @Parameterized.Parameter
  public ApiVendor apiVendor;

  @Parameterized.Parameter(1)
  public ParserMode parserMode;

  @Parameterized.Parameter(2)
  public ApiSpecification api;

  @Parameterized.Parameters(name = "{0} - {1}")
  public static Collection apiSpecifications() {
    ApiReference ramlApiRef = ApiReference.create("union-types/api.raml");
    ApiReference oas30apiRef = ApiReference.create("union-types/api.yaml");
    ParserService parserService = new ParserService();
    return asList(new Object[][] {
        {ApiVendor.RAML, ParserMode.AMF, parserService.parse(ramlApiRef, ParserMode.AMF).get()},
        {ApiVendor.RAML, ParserMode.RAML, parserService.parse(ramlApiRef, ParserMode.RAML).get()},
        {ApiVendor.OAS_30, ParserMode.AMF, parserService.parse(oas30apiRef, ParserMode.AMF).get()}
    });
  }

  @Test
  public void testBodyUnion() {
    MimeType mimeType = api.getResources().get(BODY_UNION_RESOURCE).getAction(POST_ACTION).getBody().get(APPLICATION_JSON);
    assertTrue(mimeType.validate("123").isEmpty());
    assertTrue(mimeType.validate("2020-01-19").isEmpty());
    List<ApiValidationResult> report = mimeType.validate("Hello World");
    assertNotNull(report);
    if (ParserMode.AMF.equals(parserMode)) {
      assertEquals(2, report.size());
    } else {
      assertEquals(1, report.size());
    }
  }

  @Test
  public void testQueryParametersUnion() {
    Parameter parameter =
        api.getResources().get(QUERY_PARAM_UNION_RESOURCE).getAction(GET_ACTION).getQueryParameters().get(PARAM_VALUE);
    assertTrue(parameter.validate("123"));
    assertTrue(parameter.validate("2020-01-19"));
    assertFalse(parameter.validate("Hello%20World"));
  }

  @Test
  public void testUriParametersUnion() {
    Parameter parameter =
        api.getResources().get(URI_PARAM_UNION_RESOURCE).getAction(GET_ACTION).getResolvedUriParameters().get(PARAM_VALUE);
    assertTrue(parameter.validate("123"));
    assertTrue(parameter.validate("2020-01-19"));
    assertFalse(parameter.validate("HelloWorld"));
  }

}
