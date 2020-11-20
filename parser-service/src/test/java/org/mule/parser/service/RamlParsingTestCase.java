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
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mule.apikit.loader.ApiSyncResourceLoader;
import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.internal.ParserService;
import org.mule.parser.service.result.internal.ParseResult;

import java.net.URL;
import java.util.stream.Collectors;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mule.parser.service.ParserMode.AMF;
import static org.mule.parser.service.ParserMode.RAML;

@RunWith(Parameterized.class)
public class RamlParsingTestCase {

  private static final String APISYNC_NOTATION = "resource::org.mule.parser:references:1.0.0:raml:zip:";

  private ParserService service = new ParserService();

  @Parameter(0)
  public ParserMode mode;

  @Parameter(1)
  public String versionNumber;

  @Parameters(name = "Parser {0} - RAML Version {1}")
  public static Iterable<Object[]> data() {
    return asList(new Object[][] {
        {RAML, "08"},
        {RAML, "10"},
        {AMF, "10"}
    });
  }

  @Test
  public void parseFileWithSpacesInName() {
    ParseResult result = service.parse(ApiReference.create("api-with-spaces/" + versionNumber + "/api spaces.raml"), mode);
    if (!result.success()) {
      fail(result.getErrors().stream().map(Object::toString).collect(Collectors.joining("\n")));
    }
    assertThat(result.get().getLocation(), containsString("api spaces.raml"));
    assertThat(result.get().getAllReferences(), hasSize(0));
  }

  @Test
  public void parseApiSyncWithSpacesInName() {
    String relativePath = "api-with-spaces/" + versionNumber + "/api spaces.raml";
    ResourceLoader loaderMock = resourceLoaderMock(relativePath, "api spaces.raml");
    String apisyncRaml = APISYNC_NOTATION + "api spaces.raml";
    ResourceLoader resourceLoader = new ApiSyncResourceLoader(apisyncRaml, loaderMock);
    ParseResult result = service.parse(ApiReference.create(apisyncRaml, resourceLoader), mode);
    if (!result.success()) {
      fail(result.getErrors().stream().map(Object::toString).collect(Collectors.joining("\n")));
    }
    assertThat(result.success(), is(true));
  }

  private ResourceLoader resourceLoaderMock(String resourceName, String apisyncResource) {
    try {
      ResourceLoader resourceLoaderMock = mock(ResourceLoader.class);
      ClassLoader CLL = currentThread().getContextClassLoader();
      doReturn(CLL.getResourceAsStream(resourceName))
          .when(resourceLoaderMock).getResourceAsStream(APISYNC_NOTATION + apisyncResource);
      doReturn(getResource(resourceName).toURI())
          .when(resourceLoaderMock).getResource(APISYNC_NOTATION + apisyncResource);
      return resourceLoaderMock;
    } catch (Exception e) {
      throw new RuntimeException("Something went wrong in the test: " + e.getMessage(), e);
    }
  }

  private URL getResource(String res) {
    return currentThread().getContextClassLoader().getResource(res);
  }
}
