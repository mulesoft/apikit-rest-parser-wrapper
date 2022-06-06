/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.common;

import org.junit.Test;
import org.mule.apikit.model.ApiVendor;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class ApiVendorUtilsTest {

  private static final String BASE_PATH = "api-vendor-utils-test-resources/";

  @Test
  public void getApiVendorTest() throws Exception {
    assertApiVendor("openapi.yaml", ApiVendor.OAS_30);
    assertApiVendor("openapi.json", ApiVendor.OAS_30);
    assertApiVendor("raml08.raml", ApiVendor.RAML_08);
    assertApiVendor("raml10.raml", ApiVendor.RAML_10);
    assertApiVendor("swagger.json", ApiVendor.OAS_20);
    assertApiVendor("swagger.json", ApiVendor.OAS_20);
    assertApiVendor("openapi-unindented.json", ApiVendor.OAS_30);
  }

  public void assertApiVendor(String api, ApiVendor apiVendor) {
    ApiVendor actual = getApiVendor(BASE_PATH + api);
    assertEquals(apiVendor, actual);
  }

  private static ApiVendor getApiVendor(String path) {
    InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    return ApiVendorUtils.deduceApiVendor(inputStream);
  }

}
