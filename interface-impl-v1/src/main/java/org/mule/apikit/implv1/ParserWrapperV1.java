/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1;

import static com.google.common.collect.ImmutableList.builder;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static org.mule.apikit.common.ApiSyncUtils.isSyncProtocol;

import org.mule.apikit.ApiParser;
import org.mule.apikit.implv1.loader.ApiSyncResourceLoader;
import org.mule.apikit.implv1.model.RamlImplV1;
import org.mule.apikit.implv1.parser.rule.ApiValidationResultImpl;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.validation.ApiValidationReport;
import org.mule.apikit.validation.ApiValidationResult;
import org.mule.apikit.validation.DefaultApiValidationReport;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.raml.model.Raml;
import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.loader.DefaultResourceLoader;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;

public class ParserWrapperV1 implements ApiParser {

  public static final ResourceLoader DEFAULT_RESOURCE_LOADER = new DefaultResourceLoader();

  private final String ramlPath;
  private final ResourceLoader resourceLoader;

  public ParserWrapperV1(String ramlPath) {
    this(ramlPath, emptyList());
  }

  public ParserWrapperV1(String ramlPath, List<ResourceLoader> loaders) {
    this.ramlPath = findRamlPath(ramlPath).orElse(ramlPath);
    this.resourceLoader = new CompositeResourceLoader(builder().addAll(loaders)
                                                        .add(getResourceLoaderForPath(ramlPath))
                                                        .build().toArray(new ResourceLoader[0]));
  }

  public static ResourceLoader getResourceLoaderForPath(String ramlPath) {
    if (isSyncProtocol(ramlPath)) {
      return new ApiSyncResourceLoader(ramlPath);
    }
    FileResourceLoader fileResourceLoader = new FileResourceLoader(new File(ramlPath).getParentFile());
    return new CompositeResourceLoader(DEFAULT_RESOURCE_LOADER, fileResourceLoader);
  }

  @Override
  public ApiValidationReport validate() {
    List<org.raml.parser.rule.ValidationResult> results = RamlValidationService.createDefault(resourceLoader).validate(ramlPath);
    List<ApiValidationResult> validationResults = results.stream().map(ApiValidationResultImpl::new).collect(toList());
    return new DefaultApiValidationReport(validationResults);
  }

  @Override
  public ApiSpecification parse() {
    RamlDocumentBuilder builder = new RamlDocumentBuilder(resourceLoader);
    Raml api = builder.build(ramlPath);
    return new RamlImplV1(api, resourceLoader, ramlPath);
  }

  private static Optional<String> findRamlPath(String ramlPath) {
    try {
      URL url = Thread.currentThread().getContextClassLoader().getResource(ramlPath);
      if (url != null && "file".equals(url.getProtocol())) {
        return Optional.of(url.toURI().getPath());
      }
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    return empty();
  }
}
