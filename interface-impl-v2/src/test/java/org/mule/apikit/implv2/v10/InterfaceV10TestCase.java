/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v10;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.mule.apikit.implv2.ParserV2Utils;
import org.mule.apikit.implv2.ParserWrapperV2;
import org.mule.apikit.implv2.loader.ExchangeDependencyResourceLoader;
import org.mule.apikit.model.ApiSpecification;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.raml.v2.api.loader.CompositeResourceLoader;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class InterfaceV10TestCase {

  private static final DefaultResourceLoader DEFAULT_RESOURCE_LOADER = new DefaultResourceLoader();

  @Test
  public void check() {
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    ApiSpecification raml = ParserV2Utils.build(resourceLoader, "org/mule/apikit/implv2/v10/full-1.0.raml");
    assertThat(raml.getVersion(), is("1.0"));
    assertThat(raml.getSchemas().get(0).size(), is(2));
    assertThat(raml.getSchemas().get(0).get("User"),
               is("{\"$ref\":\"#/definitions/User\",\"definitions\":{\"User\":{\"type\":\"object\",\"properties\":{\"firstname\":{\"type\":\"string\"},\"lastname\":{\"type\":\"string\"},\"age\":{\"type\":\"number\"}},\"required\":[\"firstname\",\"lastname\",\"age\"]}},\"$schema\":\"http://json-schema.org/draft-04/schema#\"}"));
    assertThat(raml.getSchemas().get(0).get("UserJson"), CoreMatchers.containsString("firstname"));
  }

  @Test
  public void references() {
    final String relativePath = "org/mule/apikit/implv2/v10/references/api.raml";
    final String pathAsUri = requireNonNull(getClass().getClassLoader().getResource(relativePath)).toString();
    final String absoulutPath = pathAsUri.substring(5);
    final List<String> paths = Arrays.asList(relativePath, pathAsUri, absoulutPath);
    paths.forEach(p -> checkReferences(p, DEFAULT_RESOURCE_LOADER));
  }

  private void checkReferences(String path, ResourceLoader resourceLoader) {
    System.out.println("Processing file = " + path);
    ApiSpecification raml = ParserV2Utils.build(resourceLoader, path);

    List<String> allReferences = raml.getAllReferences();
    allReferences.forEach(ref -> assertThat("Invalid URI", URI.create(ref).toString(), is(ref)));
    assertEquals(6, allReferences.size());

    assertThat(allReferences.stream()
        .anyMatch(r -> endWithAndExists(r, "org/mule/apikit/implv2/v10/references/address.raml", resourceLoader)), is(true));
    assertThat(allReferences.stream()
        .anyMatch(r -> endWithAndExists(r, "org/mule/apikit/implv2/v10/references/company-example.json", resourceLoader)),
               is(true));
    assertThat(allReferences.stream()
        .anyMatch(r -> endWithAndExists(r, "org/mule/apikit/implv2/v10/references/partner.raml", resourceLoader)), is(true));
    assertThat(allReferences.stream()
        .anyMatch(r -> endWithAndExists(r, "org/mule/apikit/implv2/v10/references/data-type.raml", resourceLoader)), is(true));
    assertThat(allReferences.stream()
        .anyMatch(r -> endWithAndExists(r, "org/mule/apikit/implv2/v10/references/library.raml", resourceLoader)), is(true));
    assertThat(allReferences.stream()
        .anyMatch(r -> endWithAndExists(r, "org/mule/apikit/implv2/v10/references/company.raml", resourceLoader)), is(true));
  }

  @Test
  public void referencesWithExchangeModule() throws URISyntaxException {
    final String ramlPath = "org/mule/apikit/implv2/v10/exchange/api.raml";
    File ramlFile = Paths.get(Thread.currentThread().getContextClassLoader().getResource(ramlPath).toURI()).toFile();
    CompositeResourceLoader loader = new CompositeResourceLoader(DEFAULT_RESOURCE_LOADER,
            new ExchangeDependencyResourceLoader(ramlFile.getParentFile()));
    List<String> allReferences = new ParserWrapperV2(ramlFile.getAbsolutePath()).parse().getAllReferences();
    assertEquals(3, allReferences.size());

    anyEndsWithAndExists(loader, allReferences, "org/mule/apikit/implv2/v10/exchange/exchange_modules/library1.raml");
    anyEndsWithAndExists(loader, allReferences, "org/mule/apikit/implv2/v10/exchange/exchange_modules/library2.raml");
    anyEndsWithAndExists(loader, allReferences, "org/mule/apikit/implv2/v10/exchange/exchange_modules/library3.raml");
  }

  private void anyEndsWithAndExists(CompositeResourceLoader resourceLoader, List<String> allReferences, String end) {
    boolean match = allReferences.stream()
            .anyMatch(r -> endWithAndExists(r, end.replace("/", File.separator), resourceLoader));
    assertThat(match, is(true));
  }

  @Test
  public void absoluteIncludes() {
    URL resource = getClass().getClassLoader().getResource("org/mule/apikit/implv2/v10/library-references-absolute/input.raml");
    ApiSpecification raml = ParserV2Utils.build(DEFAULT_RESOURCE_LOADER, resource.toString());

    List<String> references = raml.getAllReferences();
    assertReference(references, "org/mule/apikit/implv2/v10/library-references-absolute/libraries/resourceTypeLibrary.raml");
    assertReference(references, "org/mule/apikit/implv2/v10/library-references-absolute/libraries/typeLibrary.raml");
    assertReference(references, "org/mule/apikit/implv2/v10/library-references-absolute/libraries/traitsLibrary.raml");
    assertReference(references, "org/mule/apikit/implv2/v10/library-references-absolute/traits/trait.raml");
    assertThat(raml.getAllReferences().size(), is(4));
  }

  private void assertReference(List<String> references, String s) {
    assertThat(references.stream().anyMatch(ref -> ref.endsWith(s)), is(true));
  }

  private void assertReference(List<String> references, String assertingRef, String goldenFile, ResourceLoader loader) {
    assertThat(references.stream().anyMatch(ref -> endWithAndExists(assertingRef, goldenFile, loader)), is(true));
  }

  private boolean endWithAndExists(String reference, String goldenFile, ResourceLoader resourceLoader) {
    return reference.endsWith(goldenFile) && resourceLoader.fetchResource(reference) != null;
  }
}
