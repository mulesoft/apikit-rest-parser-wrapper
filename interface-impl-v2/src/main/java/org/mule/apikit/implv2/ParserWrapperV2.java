/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2;

import static java.util.Optional.ofNullable;
import static org.mule.apikit.common.ApiSyncUtils.isSyncProtocol;

import org.mule.apikit.ApiParser;
import org.mule.apikit.implv2.loader.ApiSyncResourceLoader;
import org.mule.apikit.implv2.loader.ExchangeDependencyResourceLoader;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.validation.ApiValidationReport;
import org.mule.apikit.validation.ApiValidationResult;
import org.mule.apikit.validation.DefaultApiValidationReport;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.raml.v2.api.loader.CompositeResourceLoader;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.FileResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.api.loader.RootRamlFileResourceLoader;

public class ParserWrapperV2 implements ApiParser {

  private static final ResourceLoader DEFAULT_RESOURCE_LOADER = new DefaultResourceLoader();

  private final String ramlPath;
  private final ResourceLoader resourceLoader;

  public ParserWrapperV2(String ramlPath) {
    this(ramlPath, getResourceLoaderForPath(ramlPath));
  }

  public ParserWrapperV2(String ramlPath, ResourceLoader resourceLoader) {
    this.ramlPath = ramlPath;
    this.resourceLoader = resourceLoader;
  }

  public ParserWrapperV2(String ramlPath, ResourceLoader... resourceLoader) {
    this(ramlPath, new CompositeResourceLoader(resourceLoader));
  }

  public static ResourceLoader getResourceLoaderForPath(String ramlPath) {
    final File ramlFile = fetchRamlFile(ramlPath);

    if (ramlFile != null && ramlFile.getParent() != null) {
      final File ramlFolder = ramlFile.getParentFile();
      return new CompositeResourceLoader(new RootRamlFileResourceLoader(ramlFolder),
                                         DEFAULT_RESOURCE_LOADER,
                                         new FileResourceLoader(ramlFolder.getAbsolutePath()),
                                         new ExchangeDependencyResourceLoader());
    } else if (isSyncProtocol(ramlPath)) {
      return new ApiSyncResourceLoader(ramlPath);
    }

    return new CompositeResourceLoader(DEFAULT_RESOURCE_LOADER, new ExchangeDependencyResourceLoader());
  }

  private static File fetchRamlFile(String ramlPath) {
    return ofNullable(ramlPath)
        .map(p -> Thread.currentThread().getContextClassLoader().getResource(p))
        .filter(ParserWrapperV2::isFile)
        .map(resource -> new File(resource.getFile()))
        .orElse(null);
  }

  private static boolean isFile(URL url) {
    return "file".equals(url.getProtocol());
  }

  @Override
  public ApiValidationReport validate() {
    List<ApiValidationResult> results = ParserV2Utils.validate(resourceLoader, ramlPath);
    return new DefaultApiValidationReport(results);
  }

  @Override
  public ApiSpecification parse() {
    return ParserV2Utils.build(resourceLoader, ramlPath);
  }
}
