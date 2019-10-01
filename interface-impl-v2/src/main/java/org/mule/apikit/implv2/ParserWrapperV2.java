/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.mule.apikit.common.ApiSyncUtils.isSyncProtocol;

import org.mule.apikit.ApiParser;
import org.mule.apikit.implv2.loader.ApiSyncResourceLoader;
import org.mule.apikit.implv2.loader.ExchangeDependencyResourceLoader;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.validation.ApiValidationReport;
import org.mule.apikit.validation.ApiValidationResult;
import org.mule.apikit.validation.DefaultApiValidationReport;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import org.raml.v2.api.loader.CompositeResourceLoader;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.FileResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.api.loader.RootRamlFileResourceLoader;

public class ParserWrapperV2 implements ApiParser {

  private static final ResourceLoader DEFAULT_RESOURCE_LOADER = new DefaultResourceLoader();

  private final String originalPath;
  private final String ramlPath;
  private final ResourceLoader resourceLoader;
  private final List<String> references;

  public ParserWrapperV2(String ramlPath, List<String> references) {
    this(ramlPath, emptyList(), references);
  }

  public ParserWrapperV2(String ramlPath, List<ResourceLoader> resourceLoader, List<String> references) {
    this.originalPath = ramlPath;
    this.ramlPath = fetchRamlResource(ramlPath).map(File::getPath).orElse(ramlPath);
    this.references = references;
    List<ResourceLoader> loaders = ImmutableList.<ResourceLoader>builder()
      .add(getResourceLoaderForPath(this.ramlPath))
      .addAll(resourceLoader)
      .build();
    this.resourceLoader = new CompositeResourceLoader(loaders.toArray(new ResourceLoader[0]));
  }

  public static ResourceLoader getResourceLoaderForPath(String ramlPath) {
    final File ramlFolder = fetchRamlFolder(ramlPath);

    if (ramlFolder != null) {
      return new CompositeResourceLoader(new RootRamlFileResourceLoader(ramlFolder),
                                         DEFAULT_RESOURCE_LOADER,
                                         new FileResourceLoader(ramlFolder.getAbsolutePath()),
                                         new ExchangeDependencyResourceLoader(ramlFolder));
    } else if (isSyncProtocol(ramlPath)) {
      return new ApiSyncResourceLoader(ramlPath);
    }

    return new CompositeResourceLoader(DEFAULT_RESOURCE_LOADER, new ExchangeDependencyResourceLoader());
  }

  private static File fetchRamlFolder(String ramlPath) {
    return fetchRamlResource(ramlPath).orElseGet(() -> {
      File file = new File(ramlPath);
      return file.exists() && file.getParent() != null ? file.getParentFile() : null;
    });
  }

  private static Optional<File> fetchRamlResource(String ramlPath) {
    if (!isSyncProtocol(ramlPath)) {
      try {
        URL url = Thread.currentThread().getContextClassLoader().getResource(ramlPath);
        if (url != null && "file".equals(url.getProtocol())) {
          return Optional.of(Paths.get(url.toURI()).toFile());
        }
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }
    return empty();
  }

  @Override
  public ApiValidationReport validate() {
    List<ApiValidationResult> results = ParserV2Utils.validate(resourceLoader, ramlPath);
    return new DefaultApiValidationReport(results);
  }

  @Override
  public ApiSpecification parse() {
    return ParserV2Utils.build(resourceLoader, originalPath, references);
  }
}
