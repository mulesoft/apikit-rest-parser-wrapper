/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import static java.lang.Thread.currentThread;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.mule.parser.service.ParserMode.RAML;

import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.result.ParseResult;
import org.mule.parser.service.result.ParsingIssue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

public class GetAllReferences08TestCase {

  private static final String API_FOLDER_NAME = "api-with-references/08/";
  private static final String MAIN_API_FILE_NAME = "api.raml";
  private static final String API_RELATIVE_PATH = API_FOLDER_NAME + MAIN_API_FILE_NAME;

  @Test
  public void getAllReferencesWithRelativePathRoot() {
    assertReferences(ApiReference.create(API_RELATIVE_PATH));
  }

  @Test
  public void getAllReferencesWithRamlFromUri() throws URISyntaxException {
    URI uri = getResource(API_RELATIVE_PATH).toURI();
    assertReferences(ApiReference.create(uri));
  }

  @Test
  public void getAllReferencesWithAbsolutePathRoot() throws URISyntaxException {
    String path = Paths.get(getResource(API_RELATIVE_PATH).toURI()).toString();
    assertReferences(ApiReference.create(path));
  }

  private void assertReferences(ApiReference api) {
    String apiFolder = new File(getResource(API_RELATIVE_PATH).getFile()).getParent();
    ParseResult parse = RAML.getStrategy().parse(api);
    if (!parse.success()) {
      throw new RuntimeException("Test failed: " + parse.getErrors().stream().map(ParsingIssue::toString).collect(joining("\n")));
    }
    List<String> refs = parse.get().getAllReferences();
    assertThat(refs, hasSize(2));
    assertThat(refs, hasItems(Paths.get(apiFolder, "include documentation.raml").toString(),
            Paths.get(apiFolder, "resource-type.yaml").toString()));
  }

  private URL getResource(String res) {
    return currentThread().getContextClassLoader().getResource(res);
  }
}
