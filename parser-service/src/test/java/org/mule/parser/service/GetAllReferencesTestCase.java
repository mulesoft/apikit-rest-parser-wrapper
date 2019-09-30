/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertTrue;
import static org.mule.parser.service.ParserMode.AUTO;

import org.mule.parser.service.util.ApiSyncTestClassLoader;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mule.apikit.common.ReferencesUtils;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.result.ParseResult;

@RunWith(Parameterized.class)
public class GetAllReferencesTestCase {

  public ParserMode mode = AUTO;

  @Parameter
  public String apiPath;


  @Parameters(name = "API = {0}")
  public static Iterable<Object[]> data() {
    return asList(new Object[][] {
        {"apis-with-references/api-simple/08/api.raml"},
        {"apis-with-references/api-simple/10/api.raml"},
        {"apis-with-references/api-with-absolute-references/08/api.raml"},
        {"apis-with-references/api-with-absolute-references/10/api.raml"},
        {"apis-with-references/api-with-exchange/08/api.raml"},
        {"apis-with-references/api-with-exchange/10/api.raml"},
        {"apis-with-references/api-with-references/08/api.raml"},
        {"apis-with-references/api-with-references/10/api.raml"},
        {"apis-with-references/api-with-spaces/08/api spaces.raml"},
        {"apis-with-references/api-with-spaces/10/api spaces.raml"}
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
    assertReferences(format("resource::%s:%s:%s:raml:zip:%s", groups[0], groups[1], groups[2], groups[3]));
  }

  @Before
  public void setUp() {
    Thread.currentThread().setContextClassLoader(new ApiSyncTestClassLoader());
  }

  private void assertReferences(String apiLocation) throws Exception {
    ParseResult raml = mode.getStrategy().parse(ApiReference.create(apiLocation));
    if (!raml.success()) {
      System.out.println(raml.getErrors().stream().map(e -> e.toString()).collect(Collectors.joining("\n")));
    }
    List<URI> refs = raml.get().getAllReferences().stream().map(ReferencesUtils::toURI).collect(toList());
    List<URI> expected = getAllReferencesExpected(apiPath);
    for (URI uri: expected) {
      assertTrue(uri.toString(), refs.contains(uri));
    }
  }

  private URL getResource(String res) {
    return currentThread().getContextClassLoader().getResource(res);
  }

  private List<URI> getAllReferencesExpected(String ramlPath) throws Exception {
    return Files.walk(Paths.get(new File(getResource(ramlPath).toURI().resolve(".").getPath()).getAbsolutePath()))
        .filter(Files::isRegularFile)
        .filter(file -> !file.getFileName().toString().endsWith(".zip"))
        .filter(file -> !file.getFileName().toString().endsWith("api.raml"))
        .filter(file -> !file.getFileName().toString().endsWith("api spaces.raml"))
        // TODO : amf bug missing xsd imports
        .filter(file -> !file.getFileName().toString().endsWith("namespace.xsd"))
        // TODO : amf bug missing example declared in type 08
        .filter(file -> !file.getFileName().toString().endsWith("generic_error.xml"))
        .map(file -> file.toUri())
        .collect(toList());
  }
}
