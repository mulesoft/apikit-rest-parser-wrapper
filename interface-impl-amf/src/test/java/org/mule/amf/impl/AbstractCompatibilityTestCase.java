/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import org.junit.Assert;
import org.mule.apikit.ApiParser;
import org.mule.apikit.implv1.ParserWrapperV1;
import org.mule.apikit.implv2.ParserWrapperV2;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.api.ApiReference;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertNotNull;

abstract class AbstractCompatibilityTestCase extends AbstractTestCase {

  protected ApiSpecification amf;
  protected ApiSpecification raml;
  protected final boolean isRaml08;

  protected ApiParser ramlWrapper;
  protected ApiParser amfWrapper;

  protected File input;

  private static final PathMatcher API_MATCHER = FileSystems.getDefault().getPathMatcher("glob:api.raml");

  AbstractCompatibilityTestCase(final File input, final String name) {
    this.input = input;
    final URI uri = input.toURI();
    final String apiPath = uri.toString();

    isRaml08 = isRaml08(input);
    // Create Java Parser Wrapper
    ramlWrapper = createJavaParserWrapper(apiPath, isRaml08);
    raml = ramlWrapper.parse();
    assertNotNull(raml);

    // Create AMF Wrapper
    try {
      amfWrapper = new AMFParser(ApiReference.create(uri), true);
      amf = amfWrapper.parse();
      assertNotNull(amf);
    } catch (Exception e) {
      Assert.fail(e.getMessage());
      e.printStackTrace();
    }
  }

  static Collection<Object[]> getData(final URI baseFolder) throws IOException {
    final List<File> apis = scan(baseFolder);

    final List<Object[]> parameters = new ArrayList<>();
    apis.forEach(api -> parameters.add(new Object[] {api, api.getParentFile().getName()}));

    return parameters;
  }

  private static List<File> scan(final URI resources) throws IOException {

    return Files.walk(Paths.get(resources))
        .filter(path -> Files.isRegularFile(path) && API_MATCHER.matches(path.getFileName()))
        .map(Path::toFile)
        .collect(toList());
  }

  private static boolean isRaml08(final File file) {
    boolean result = false;
    try {
      final Stream<String> lines = Files.lines(file.toPath());
      final Optional<String> first = lines.findFirst();
      if (first.isPresent())
        result = first.get().contains("#%RAML 0.8");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  static ApiParser createJavaParserWrapper(final String apiPath, final boolean isRaml08) {

    final ApiParser ramlWrapper =
        isRaml08 ? new ParserWrapperV1(apiPath, Collections.emptyList()) : new ParserWrapperV2(apiPath, Collections.emptyList());
    ramlWrapper.validate();
    return ramlWrapper;
  }

}
