/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.apikit.implv1.loader;

import org.junit.Before;
import org.junit.Test;
import org.raml.parser.loader.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ApiSyncResourceLoaderTest {

  private static final String EXCHANGE_NOTATION = "exchange_modules/org.mule.parser/references/1.0.0/";
  private static final String APISYNC_NOTATION = "resource::org.mule.parser:references:1.0.0:raml-fragment:zip:";
  private static final String APIS_08_EMPTY_API_RAML = "apis/08-empty/api.raml";
  private static final String EXAMPLE_JSON = "examples/fixture-get-example.json";
  private ApiSyncResourceLoader apiSyncResourceLoader;

  @Before
  public void setUp() throws Exception {
    ResourceLoader resourceLoader = mock(ResourceLoader.class);
    doReturn(getInputStream("/" + APIS_08_EMPTY_API_RAML)).when(resourceLoader).fetchResource(APIS_08_EMPTY_API_RAML);
    doReturn(getInputStream("/apis/08-leagues/" + EXAMPLE_JSON)).when(resourceLoader)
        .fetchResource(APISYNC_NOTATION + EXAMPLE_JSON);
    apiSyncResourceLoader = new ApiSyncResourceLoader("/" + APIS_08_EMPTY_API_RAML, resourceLoader);

  }

  private InputStream getInputStream(String s) throws IOException {
    return this.getClass().getResource(s).openStream();
  }

  @Test
  public void fetchResource() {
    assertNotNull(apiSyncResourceLoader.fetchResource(APIS_08_EMPTY_API_RAML));
    assertNotNull(apiSyncResourceLoader.fetchResource("/" + APIS_08_EMPTY_API_RAML));
    assertNotNull(apiSyncResourceLoader.fetchResource(APISYNC_NOTATION + EXAMPLE_JSON));
    assertNotNull(apiSyncResourceLoader.fetchResource(EXCHANGE_NOTATION + EXAMPLE_JSON));
    assertNull(apiSyncResourceLoader.fetchResource(EXCHANGE_NOTATION + "not-valid-resource.json"));
  }
}
