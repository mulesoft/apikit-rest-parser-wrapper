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
import org.mule.apikit.common.ReferencesUtils;
import org.mule.apikit.loader.ClassPathResourceLoader;
import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.result.internal.ParseResult;
import org.mule.parser.service.util.ApiSyncTestResourceLoader;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mule.parser.service.ParserMode.AUTO;

@RunWith(Parameterized.class)
public class GetAllReferencesTestCase {

  public ParserMode mode = AUTO;

  @Parameter
  public String apiPath;


  @Parameters(name = "API = {0}")
  public static Iterable<Object[]> data() {
    return asList(new Object[][] {
        {"apis/api-simple/08/api.raml"},
        {"apis/api-simple/10/api.raml"},
        {"apis/api-with-absolute-references/08/api.raml"},
        {"apis/api-with-absolute-references/10/api.raml"},
        {"apis/api-with-exchange/08/api.raml"},
        {"apis/api-with-exchange/10/api.raml"},
        {"apis/api-with-references/08/api.raml"},
        {"apis/api-with-references/10/api.raml"},
        {"apis/api-with-spaces/08/api spaces.raml"},
        {"apis/api-with-spaces/space in path api/api.raml"},
        {"apis/api-with-spaces/10/api spaces.raml"}
    });
  }

  @Test
  public void getAllReferencesWithRelativePathRoot() throws Exception {
    assertReferences(ApiReference.create(apiPath));
  }

  @Test
  public void getAllReferencesWithRamlFromUri() throws Exception {
    URI uri = getResource(apiPath).toURI();
    assertReferences(ApiReference.create(uri.toString()));
  }

  @Test
  public void getAllReferencesWithAbsolutePathRoot() throws Exception {
    String path = new File(getResource(apiPath).toURI().getPath()).getAbsolutePath();
    assertReferences(ApiReference.create(path));
  }

  @Test
  public void getAllReferencesWithAPISync() throws Exception {
    String[] groups = apiPath.split("/");
    CompositeResourceLoader composite = new CompositeResourceLoader(new ApiSyncTestResourceLoader(),
                                                                    new ClassPathResourceLoader());
    assertReferences(ApiReference.create(format("resource::%s:%s:%s:raml:zip:%s", groups[0], groups[1], groups[2], groups[3]),
                                         composite));
  }

  private void assertReferences(ApiReference apiReference) throws Exception {
    ParseResult raml = mode.getStrategy().parse(apiReference);
    if (!raml.success()) {
      String message = raml.getErrors().stream().map(e -> e.toString())
          .collect(Collectors.joining("\n"));
      fail(message);
    }
    List<URI> refs = raml.get().getAllReferences().stream().map(ReferencesUtils::toURI).collect(toList());
    List<URI> expected = getAllReferencesExpected(apiPath);
    for (URI uri : expected) {
      assertTrue(uri.toString(), refs.contains(uri));
    }
    for (URI uri : refs) {
      assertTrue(uri.toString(), expected.contains(uri));
    }
    assertEquals(expected.size(), refs.size());
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
        .map(file -> file.toUri())
        .collect(toList());
  }

  public class CompositeResourceLoader implements ResourceLoader {

    private ResourceLoader[] resourceLoaders;

    public CompositeResourceLoader(ResourceLoader... resourceLoaders) {
      this.resourceLoaders = resourceLoaders;
    }

    @Override
    public InputStream getResourceAsStream(String res) {
      InputStream result = null;
      ResourceLoader[] var3 = this.resourceLoaders;
      int var4 = var3.length;

      for (int var5 = 0; var5 < var4; ++var5) {
        ResourceLoader loader = var3[var5];
        result = loader.getResourceAsStream(res);
        if (result != null) {
          break;
        }
      }

      return result;
    }

    @Override
    public URI getResource(String resourceName) {
      URI result = null;
      ResourceLoader[] var3 = this.resourceLoaders;
      int var4 = var3.length;

      for (int var5 = 0; var5 < var4; ++var5) {
        ResourceLoader loader = var3[var5];
        result = loader.getResource(resourceName);
        if (result != null) {
          break;
        }
      }
      return result;
    }
  }
}
