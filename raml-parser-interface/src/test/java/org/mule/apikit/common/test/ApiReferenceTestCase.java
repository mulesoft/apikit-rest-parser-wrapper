/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.common.test;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.net.URISyntaxException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.api.ApiReference;

@RunWith(Parameterized.class)
public class ApiReferenceTestCase {

  @Parameter(0)
  public String apiPath;

  @Parameter(1)
  public ApiVendor vendor;

  @Parameter(2)
  public String format;

  @Parameter(3)
  public String classifier;

  @Parameters(name = "API: {0} VENDOR: {1} FORMAT: {2}")
  public static Iterable<Object[]> data() {
    return asList(new Object[][] {
        {"oas-sync/examples/2.0/petstore.json", ApiVendor.OAS_20, "JSON", "oas"}
    });
  }

  @Test
  public void apiReferenceCreateWithAPISync() {
    String[] groups = apiPath.split("/");
    ApiReference apiReference = ApiReference
        .create(
                format("resource::%s:%s:%s:%s:zip:%s", groups[0], groups[1], groups[2], classifier, groups[3]),
                relativePath -> {
                  try {
                    String[] resource = relativePath.split(":");
                    String path = join("/", resource[2], resource[3], resource[4], resource[7]);
                    return currentThread().getContextClassLoader().getResource(path).toURI();
                  } catch (URISyntaxException e) {
                    return null;
                  }
                });
    assertThat(apiReference.getVendor(), equalTo(vendor));
    assertThat(apiReference.getFormat(), equalTo(format));
  }
}
