/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mule.apikit.model.ApiVendor.RAML_08;
import static org.mule.apikit.model.ApiVendor.RAML_10;

import org.mule.apikit.ApiParser;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.api.ApiReference;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CompatibilityTestCase extends AbstractCompatibilityTestCase {

  public CompatibilityTestCase(final File input, final String name) {
    super(input, name);
  }

  @Parameterized.Parameters(name = "{1}")
  public static Collection<Object[]> getData() throws IOException, URISyntaxException {
    final URI baseFolder = CompatibilityTestCase.class.getResource("").toURI(); // 08-resources
    return getData(baseFolder);

  }

  @Test
  public void apiVendor() {
    final ApiVendor expected = isRaml08 ? RAML_08 : RAML_10;
    assertThat(amfWrapper.parse().getApiVendor(), is(expected));
    assertThat(ramlWrapper.parse().getApiVendor(), is(expected));
  }

  @Test
  public void dump() {
    final String amfDump = amfWrapper.parse().dump("http://apikit-test");
    final String ramlDump = ramlWrapper.parse().dump("http://apikit-test");

    // Dump to file
    final Path basePath = Paths.get(input.getPath()).getParent();
    final Path amfDumpPath = basePath.resolve("amf-dump.raml");
    final Path ramlDumpPath = basePath.resolve("raml-dump.raml");

    try {
      Files.write(amfDumpPath, amfDump.getBytes("UTF-8"));
    } catch (IOException e) {
      Assert.fail("Error persisting AMF dump file");
      e.printStackTrace();
    }

    try {
      Files.write(ramlDumpPath, ramlDump.getBytes("UTF-8"));
    } catch (IOException e) {
      Assert.fail("Error persisting RAML dump file");
      e.printStackTrace();
    }

    // Parse java dumped file  
    final ApiParser dumpedRamlWrapper = createJavaParserWrapper(ramlDumpPath.toUri().toString(), isRaml08);
    final ApiSpecification dumpedRaml = dumpedRamlWrapper.parse();
    assertNotNull(dumpedRaml);

    try {
      final ApiParser dumpedAmfWrapper = new AMFParser(ApiReference.create(amfDumpPath.toUri()), true);
      final ApiSpecification dumpedAmf = dumpedAmfWrapper.parse();
      assertNotNull(dumpedAmf);
      assertEqual(dumpedAmf, dumpedRaml);
    } catch (Exception e) {
      Assert.fail("Error parsing AMF dumped file:\n" + e.getMessage());
    }
  }

  @Test
  public void raml() {
    assertEqual(amf, raml);
  }

}
