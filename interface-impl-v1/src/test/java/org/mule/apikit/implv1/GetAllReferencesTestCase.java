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
import org.mule.apikit.common.ReferencesUtils;
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
import static java.util.stream.Collectors.toList;
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
    String path = new File(getResource(apiPath).toURI().getPath()).getAbsolutePath();
    assertReferences(path);
  }

  @Test
  public void getAllReferencesWithAPISync() throws Exception {
    String[] groups = apiPath.split("/");
    // assertReferences(format("resource::%s:%s:%s:raml:zip:%s", groups[0], groups[1], groups[2], groups[3]));
  }

  @Before
  public void setUp() {
    Thread.currentThread().setContextClassLoader(new ApiSyncTestClassLoader());
  }

  private void assertReferences(String apiLocation) throws Exception {
    ApiSpecification raml = new ParserWrapperV1(apiLocation).parse();

    List<URI> refs = raml.getAllReferences().stream().map(ReferencesUtils::toURI).collect(toList());
    List<URI> expected = getAllReferencesExpected(apiPath);
    for (URI uri: expected) {
      if (isSyncProtocol(apiLocation)) {
        uri = URI.create(getApi(apiLocation)).resolve(getResource(apiPath).toURI().resolve(".").relativize(uri));
        assertTrue(uri.getPath(), refs.contains(uri));
      } else {
        assertTrue(uri.getPath(), refs.contains(uri));
      }
    }
  }

  private URL getResource(String res) {
    return currentThread().getContextClassLoader().getResource(res);
  }

  private List<URI> getAllReferencesExpected(String ramlPath) throws Exception {
    return Files.walk(Paths.get(new File(getResource(ramlPath).toURI().resolve(".").getPath()).getAbsolutePath()))
        .filter(Files::isRegularFile)
        .filter(file -> !file.getFileName().toString().endsWith(".zip"))
        .map(file -> file.toUri())
        .collect(toList());
  }
}
