/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.apikit.model.ApiVendor;

import java.io.InputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mule.apikit.common.ApiVendorUtils.deduceApiVendor;

@RunWith(Parameterized.class)
public class ApiVendorUtilsTest {

  private static final String BASE_PATH = "api-vendor-utils-test-resources/";

  private final String testName;
  private final ApiVendor expectedVendor;

  public ApiVendorUtilsTest(String testName, ApiVendor expectedVendor) {
    this.testName = testName;
    this.expectedVendor = expectedVendor;
  }

  @Parameterized.Parameters(name = "{index}: deduce({0})={1}")
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {"openapi.yaml", ApiVendor.OAS_30},
        {"openapi.json", ApiVendor.OAS_30},
        {"raml08.raml", ApiVendor.RAML_08},
        {"raml10.raml", ApiVendor.RAML_10},
        {"swagger.json", ApiVendor.OAS_20},
        {"openapi-unindented.json", ApiVendor.OAS_30},
    });
  }

  @Test
  public void assertApiVendor() {
    ApiVendor actualVendor = getApiVendor(BASE_PATH + testName);
    assertEquals(expectedVendor, actualVendor);
  }

  private static ApiVendor getApiVendor(String path) {
    InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    return deduceApiVendor(inputStream);
  }

}
