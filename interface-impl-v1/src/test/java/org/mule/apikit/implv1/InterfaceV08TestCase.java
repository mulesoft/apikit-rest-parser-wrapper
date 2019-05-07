/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1;

import org.junit.Test;
import org.mule.apikit.model.ApiSpecification;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mule.apikit.implv1.ParserWrapperV1.DEFAULT_RESOURCE_LOADER;

public class InterfaceV08TestCase {

  @Test
  public void references() {
    final String relativePath = "org/mule/apikit/implv1/api.raml";
    final String pathAsUri = requireNonNull(getClass().getClassLoader().getResource(relativePath)).toString();
    final String absoulutPath = pathAsUri.substring(5);
    final List<String> paths = Arrays.asList(relativePath, pathAsUri, absoulutPath);
    paths.forEach(this::checkReferences);
  }

  private void checkReferences(String path) {
    System.out.println("Processing file = " + path);
    ApiSpecification raml = new ParserWrapperV1(path).parse();

    List<String> allReferences = raml.getAllReferences();
    allReferences.forEach(ref -> assertThat("Invalid URI", URI.create(ref).toString(), is(ref)));
    assertEquals(9, allReferences.size());

    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/traits/versioned.raml"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/resourceTypes/base.raml"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/traits/collection.raml"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/resourceTypes/../examples/generic_error.xml"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/schemas/atom.xsd"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/resourceTypes/emailed.raml"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/securitySchemes/oauth_2_0.raml"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/securitySchemes/oauth_1_0.raml"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/traits/override-checked.raml"), is(true));
  }

  private boolean anyMatch(List<String> allReferences, String s) {
    return allReferences.stream().anyMatch(p -> endWithAndExists(p, s));
  }

  private boolean endWithAndExists(String reference, String goldenFile) {
    return reference.endsWith(goldenFile) && DEFAULT_RESOURCE_LOADER.fetchResource(reference) != null;
  }
}
