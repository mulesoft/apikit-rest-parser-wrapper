/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.apikit.implv2.v10.loader;

import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.implv2.loader.ApiSyncResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ApiSyncResourceLoaderTest {

  private static final String EXCHANGE_NOTATION = "exchange_modules/org.mule.parser/references/1.0.0/";
  private static final String APISYNC_NOTATION = "resource::org.mule.parser:references:1.0.0:raml-fragment:zip:";
  private static final String APIS_10_LEAGUES_API_RAML = "/apis/10-leagues/api.raml";
  private ApiSyncResourceLoader apiSyncResourceLoader;

  @Before
  public void setUp() throws Exception {
    ResourceLoader resourceLoader = mock(ResourceLoader.class);
    doReturn(getInputStream(APIS_10_LEAGUES_API_RAML)).when(resourceLoader).fetchResource("api.raml");
    doReturn(getInputStream("/apis/10-leagues/league.json")).when(resourceLoader).fetchResource(APISYNC_NOTATION + "league.json");
    apiSyncResourceLoader = new ApiSyncResourceLoader(APIS_10_LEAGUES_API_RAML, resourceLoader);

  }

  private InputStream getInputStream(String s) throws IOException {
    return this.getClass().getResource(s).openStream();
  }

  @Test
  public void fetchResourceTest() {
    assertNotNull(apiSyncResourceLoader.fetchResource("api.raml"));
    assertNotNull(apiSyncResourceLoader.fetchResource("/api.raml"));
    assertNotNull(apiSyncResourceLoader.fetchResource(APISYNC_NOTATION + "league.json"));
    assertNotNull(apiSyncResourceLoader.fetchResource(EXCHANGE_NOTATION + "league.json"));
    assertNull(apiSyncResourceLoader.fetchResource(EXCHANGE_NOTATION + "not-valid-resource.json"));
  }

  @Test
  public void defaultResourceLoaderTest() {
    assertNotNull(new ApiSyncResourceLoader(APIS_10_LEAGUES_API_RAML).fetchResource(APIS_10_LEAGUES_API_RAML));
  }
}
