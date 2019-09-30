/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1;

import static java.util.Objects.requireNonNull;
import static org.apache.logging.log4j.core.util.ReflectionUtil.setFieldValue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.mule.apikit.implv1.ParserWrapperV1.DEFAULT_RESOURCE_LOADER;

import org.mockito.Mockito;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mule.apikit.implv1.loader.ApiSyncResourceLoader;
import org.mule.apikit.implv1.loader.SnifferResourceLoader;
import org.mule.apikit.implv1.model.RamlImplV1;
import org.mule.apikit.implv1.parser.Raml08ReferenceFinder;
import org.mule.apikit.model.ApiSpecification;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.raml.model.Raml;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;

public class InterfaceV08TestCase {

  @Test
  public void references() {
    final String relativePath = "org/mule/apikit/implv1/api.raml";
    final String pathAsUri = requireNonNull(getClass().getClassLoader().getResource(relativePath)).toString();
    final String absolutePath = pathAsUri.substring(5);
    final List<String> paths = Arrays.asList(relativePath, absolutePath, pathAsUri);
    paths.forEach(this::checkReferences);
  }

  private void checkReferences(String path) {
    ApiSpecification raml = new ParserWrapperV1(path).parse();

    List<String> allReferences = raml.getAllReferences();
    allReferences.forEach(ref -> assertThat("Invalid URI", URI.create(ref).toString(), is(ref)));
    assertEquals(11, allReferences.size());

    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/api.raml"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/traits/versioned.raml"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/resourceTypes/base.raml"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/traits/collection.raml"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/examples/generic_error.xml"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/schemas/atom.xsd"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/schemas/namespace.xsd"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/resourceTypes/emailed.raml"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/securitySchemes/oauth_2_0.raml"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/securitySchemes/oauth_1_0.raml"), is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/traits/override%20checked.raml"), is(true));
  }

  private boolean anyMatch(List<String> allReferences, String s) {
    return allReferences.stream().anyMatch(p -> endWithAndExists(p, s));
  }

  private boolean endWithAndExists(String reference, String goldenFile) {
    return reference.endsWith(goldenFile) && DEFAULT_RESOURCE_LOADER.fetchResource(reference) != null;
  }

  @Test
  public void findIncludesWithApiSyncAPI() throws Exception {
    String raml = "resource::org.mule.apikit.implv1:references:1.0.0:api.raml";
    SnifferResourceLoader loader = new SnifferResourceLoader(new ApiSyncResourceLoader(raml, mockApiSyncResources()), raml);
    List<String> allReferences = new RamlImplV1(new RamlDocumentBuilder(loader).build(raml), loader, raml).getAllReferences();
    assertEquals(11, allReferences.size());

    assertThat(allReferences.contains("resource::org.mule.apikit.implv1:references:1.0.0:/api.raml"), is(true));
    assertThat(allReferences.contains("resource::org.mule.apikit.implv1:references:1.0.0:/traits/versioned.raml"), is(true));
    assertThat(allReferences.contains("resource::org.mule.apikit.implv1:references:1.0.0:/resourceTypes/base.raml"), is(true));
    assertThat(allReferences.contains("resource::org.mule.apikit.implv1:references:1.0.0:/traits/collection.raml"), is(true));
    assertThat(allReferences.contains("resource::org.mule.apikit.implv1:references:1.0.0:/examples/generic_error.xml"), is(true));
    assertThat(allReferences.contains("resource::org.mule.apikit.implv1:references:1.0.0:/schemas/atom.xsd"), is(true));
    assertThat(allReferences.contains("resource::org.mule.apikit.implv1:references:1.0.0:/schemas/namespace.xsd"), is(true));
    assertThat(allReferences.contains("resource::org.mule.apikit.implv1:references:1.0.0:/resourceTypes/emailed.raml"), is(true));
    assertThat(allReferences.contains("resource::org.mule.apikit.implv1:references:1.0.0:/securitySchemes/oauth_2_0.raml"), is(true));
    assertThat(allReferences.contains("resource::org.mule.apikit.implv1:references:1.0.0:/securitySchemes/oauth_1_0.raml"), is(true));
    assertThat(allReferences.contains("resource::org.mule.apikit.implv1:references:1.0.0:/traits/override%20checked.raml"), is(true));
  }

  private static ResourceLoader mockApiSyncResources() throws Exception {
    ResourceLoader resourceLoaderMock = Mockito.mock(ResourceLoader.class);
    doAnswer(invocationOnMock -> {
      String relativePath = ((String) invocationOnMock.getArguments()[0]).substring(50);
      return Thread.currentThread().getClass().getResource("/org/mule/apikit/implv1/" + relativePath).openStream();
    }).when(resourceLoaderMock).fetchResource(anyString());
    return resourceLoaderMock;
  }
}
