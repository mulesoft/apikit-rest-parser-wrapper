/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.mule.raml.implv2.loader.ApiSyncResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ParserV2UtilsTestCase {

  @Test
  public void chooseWhichParserToUseWithoutSystemProperty() {
    assertTrue(ParserV2Utils.useParserV2("#%RAML 1.0 this is an api definition"));
    assertFalse(ParserV2Utils.useParserV2("#%RAML 0.8 this is an api definition"));
  }

  @Test
  public void chooseWhichParserToUseWithSystemPropertyInTrue() {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    assertTrue(ParserV2Utils.useParserV2("#%RAML 0.8 this is an api definition"));
    assertTrue(ParserV2Utils.useParserV2("#%RAML 1.0 this is an api definition"));
  }

  @Test
  public void chooseWhichParserToUseWithSystemPropertyInFalse() {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "false");
    assertTrue(ParserV2Utils.useParserV2("#%RAML 1.0 this is an api definition"));
    assertFalse(ParserV2Utils.useParserV2("#%RAML 0.8 this is an api definition"));
  }

  @Test
  public void chooseWhichParserToUseWithSystemPropertyInANonBooleanValue() {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "non-boolean-value");
    assertTrue(ParserV2Utils.useParserV2("#%RAML 1.0 this is an api definition"));
    assertFalse(ParserV2Utils.useParserV2("#%RAML 0.8 this is an api definition"));
  }


  @Test
  public void findIncludesWithApiSyncAPI() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "non-boolean-value");
    List<String> includes = ParserV2Utils.findIncludeNodes(new URI("resource::org.mule.apikit.implv2.v10:references:1.0.0:api.raml"),
                                   new ApiSyncResourceLoader("resource::org.mule.apikit.implv2.v10:references:1.0.0:api.raml",mockApiSyncResources()));
    assertEquals(6,includes.size());
  }

  private static ResourceLoader mockApiSyncResources() throws Exception {
    ResourceLoader resourceLoaderMock = Mockito.mock(ResourceLoader.class);
    mockResourceLoader("resource::org.mule.apikit.implv2.v10:references:1.0.0:api.raml",
            "/org/mule/raml/implv2/v10/references/api.raml", resourceLoaderMock);
    mockResourceLoader("resource::org.mule.apikit.implv2.v10:references:1.0.0:data-type.raml",
            "/org/mule/raml/implv2/v10/references/data-type.raml",resourceLoaderMock );
    mockResourceLoader("resource::org.mule.apikit.implv2.v10:references:1.0.0:address.raml",
            "/org/mule/raml/implv2/v10/references/address.raml",resourceLoaderMock );
    mockResourceLoader("resource::org.mule.apikit.implv2.v10:references:1.0.0:company.raml",
            "/org/mule/raml/implv2/v10/references/company.raml", resourceLoaderMock);
    mockResourceLoader("resource::org.mule.apikit.implv2.v10:references:1.0.0:company-example.json",
            "/org/mule/raml/implv2/v10/references/company-example.json",resourceLoaderMock );
    mockResourceLoader("resource::org.mule.apikit.implv2.v10:references:1.0.0:library.raml",
            "/org/mule/raml/implv2/v10/references/library.raml",resourceLoaderMock );
    mockResourceLoader("resource::org.mule.apikit.implv2.v10:references:1.0.0:partner.raml",
            "/org/mule/raml/implv2/v10/references/partner.raml", resourceLoaderMock);
    return resourceLoaderMock;
  }

  private static void mockResourceLoader(String resourceURL, String resourcePath, ResourceLoader resourceLoaderMock) throws Exception {
    Mockito.doReturn(getInputStream(resourcePath)).when(resourceLoaderMock)
            .fetchResource(resourceURL);
  }

  private static InputStream getInputStream(String resourcePath) throws IOException {
    return Thread.currentThread().getClass().getResource(resourcePath).openStream();
  }

  @After
  public void after() {
    System.clearProperty(ParserV2Utils.PARSER_V2_PROPERTY);
  }
}
