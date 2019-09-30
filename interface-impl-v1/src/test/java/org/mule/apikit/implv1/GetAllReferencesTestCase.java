/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.api.ApiReference;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mule.apikit.common.ApiSyncUtils.getApi;
import static org.mule.apikit.common.ApiSyncUtils.isSyncProtocol;

@RunWith(Parameterized.class)
public class GetAllReferencesTestCase {

  @Parameterized.Parameter
  public String apiPath;

  @Parameterized.Parameters(name = "{0}")
  public static Iterable<Object[]> data() {
    return asList(new Object[][] {
        {"apis/api-with-examples/08/api.raml"},
        {"apis/api-with-references/08/api.raml"},
        {"apis/api-with-spaces/08/api spaces.raml"}
    });
  }

  @Test
  public void getAllReferencesWithRelativePathRoot() throws Exception {
    assertReferences(apiPath);
  }

  @Test
  public void getAllReferencesWithRamlFromUri() throws Exception {
    URI uri = getResource(apiPath).toURI();
    assertReferences(uri.toString());
  }

  @Test
  public void getAllReferencesWithAbsolutePathRoot() throws Exception {
    String path = getResource(apiPath).toURI().getPath();
    assertReferences(path);
  }

  @Test
  public void getAllReferencesWithAPISync() throws Exception {
    String[] groups = apiPath.split("/");
    assertReferences(format("resource::%s:%s:%s:raml:zip:%s", groups[0], groups[1], groups[2], groups[3]));
  }

  @Before
  public void setUp() {
    Thread.currentThread().setContextClassLoader(new ApiSyncTestClassLoader());
  }

  private void assertReferences(String apiLocation) throws Exception {
    ApiSpecification raml = new ParserWrapperV1(apiLocation).parse();

    List<String> refs = raml.getAllReferences();
    List<String> expected = getAllReferencesExpected(apiPath);
    for (String uri: expected) {
      if (isSyncProtocol(apiLocation)) {
        uri = getApi(apiLocation) + "/" + getResource(apiPath).toURI().resolve(".").relativize(URI.create(uri)).toString().replace("%20", " ");
        assertTrue(uri, refs.contains(uri));
      } else {
        assertTrue(uri, refs.contains(uri));
      }
    }
  }

  private URL getResource(String res) {
    return currentThread().getContextClassLoader().getResource(res);
  }

  private List<String> getAllReferencesExpected(String ramlPath) throws Exception {
    return Files.walk(Paths.get(getResource(ramlPath).toURI().resolve(".").getPath()))
        .filter(Files::isRegularFile)
        .filter(file -> !file.getFileName().toString().endsWith(".zip"))
        .map(file -> file.toUri())
        .map(uri -> uri.toString())
        .collect(Collectors.toList());
  }
}
