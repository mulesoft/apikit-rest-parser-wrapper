/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mule.parser.service.ParserMode.AMF;
import static org.mule.parser.service.ParserMode.RAML;

import org.mule.apikit.loader.ApiSyncResourceLoader;
import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.result.ParseResult;
import org.mule.parser.service.result.ParsingIssue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class GetAllReferencesTestCase {

  private static final String API_FOLDER_NAME = "api-with-references/10/";
  private static final String MAIN_API_FILE_NAME = "api.raml";
  private static final String API_RELATIVE_PATH = API_FOLDER_NAME + MAIN_API_FILE_NAME;
  private static final String APISYNC_NOTATION = "resource::org.mule.parser:references:1.0.0:raml:zip:";
  private static final String ROOT_APISYNC_RAML = APISYNC_NOTATION + MAIN_API_FILE_NAME;

  @Parameter
  public ParserMode mode;

  @Parameters(name = "Parser = {0}")
  public static Iterable<Object[]> data() {
    return asList(new Object[][] {
      {AMF},
      {RAML}
    });
  }

  @Test
  public void getAllReferencesWithRelativePathRoot() {
    assertReferences(ApiReference.create(API_RELATIVE_PATH));
  }

  @Test
  public void getAllReferencesWithAPISync() {
    // THIS REFERENCES WONT WORK FOR RAML PARSER, RAML PARSER WITH APISYNC DOES NOT RESOLVE FULL PATHS /shrug
    assumeThat(mode, is(AMF));
    ResourceLoader resourceLoader = new ApiSyncResourceLoader(ROOT_APISYNC_RAML, resourceLoaderMock());
    assertReferences(ApiReference.create(ROOT_APISYNC_RAML, resourceLoader));
  }

  @Test
  public void getAllReferencesWithRamlFromUri() throws URISyntaxException {
    URI uri = getResource(API_RELATIVE_PATH).toURI();
    assertReferences(ApiReference.create(uri));
  }

  @Test
  public void getAllReferencesWithAbsolutePathRoot() {
    String path = getResource(API_RELATIVE_PATH).getFile();
    assertReferences(ApiReference.create(path));
  }

  private void assertReferences(ApiReference api) {
    String apiFolder = new File(getResource(API_RELATIVE_PATH).getFile()).getParent();
    ParseResult parse = mode.getStrategy().parse(api);
    if (!parse.success()) {
      throw new RuntimeException("Test failed: " + parse.getErrors().stream().map(ParsingIssue::toString).collect(joining("\n")));
    }
    List<String> refs = parse.get().getAllReferences();
    assertThat(refs, hasSize(6));
    assertThat(refs, hasItems(apiFolder + "/partner with spaces.raml",
                              apiFolder + "/data-type.raml",
                              apiFolder + "/library.raml",
                              apiFolder + "/company.raml",
                              apiFolder + "/address.raml",
                              apiFolder + "/company-example.json"));
  }

  private ResourceLoader resourceLoaderMock() {
    ResourceLoader resourceLoaderMock = mock(ResourceLoader.class);
    List<String> relativePaths = Arrays.asList(MAIN_API_FILE_NAME,
                                               "company.raml",
                                               "company-example.json",
                                               "data-type.raml",
                                               "library.raml",
                                               "partner with spaces.raml",
                                               "address.raml");
    ClassLoader CLL = currentThread().getContextClassLoader();
    try {
      for (String relativePath : relativePaths) {
        String apisyncResource = API_FOLDER_NAME + relativePath;
        String apisyncRelativePath = APISYNC_NOTATION + relativePath;
        doReturn(CLL.getResourceAsStream(apisyncResource)).when(resourceLoaderMock).getResourceAsStream(apisyncRelativePath);
        doReturn(getResource(apisyncResource).toURI()).when(resourceLoaderMock).getResource(apisyncRelativePath);
      }
      return resourceLoaderMock;
    } catch (Exception e) {
      throw new RuntimeException("Something went wrong in the test: " + e.getMessage(), e);
    }
  }

  private URL getResource(String res) {
    return currentThread().getContextClassLoader().getResource(res);
  }
}
