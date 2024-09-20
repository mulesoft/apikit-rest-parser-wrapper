/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.loader;

import amf.core.client.common.remote.Content;
import amf.core.client.platform.execution.ExecutionEnvironment;
import amf.core.client.platform.resource.FileResourceLoader;
import amf.core.client.platform.resource.ResourceLoader;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExchangeDependencyResourceLoader implements ResourceLoader {

  private final File workingDir;
  private final FileResourceLoader resourceLoader;

  private static final Pattern DEPENDENCY_PATH_PATTERN = Pattern.compile("^exchange_modules/|/exchange_modules/");

  public ExchangeDependencyResourceLoader(String rootDir, ExecutionEnvironment executionEnvironment) {
    String basePath = rootDir != null ? rootDir : ".";
    this.workingDir = new File(basePath);
    this.resourceLoader = new FileResourceLoader(executionEnvironment);
  }

  @Override
  public CompletableFuture<Content> fetch(String path) {
    final CompletableFuture<Content> future = new CompletableFuture<>();
    if (path == null || path.isEmpty()) {
      future.completeExceptionally(new Exception("Failed to apply."));
      return future;
    }

    final Matcher matcher = DEPENDENCY_PATH_PATTERN.matcher(path);
    if (matcher.find()) {

      final int dependencyIndex = path.lastIndexOf(matcher.group(0));
      final String resourceName = dependencyIndex <= 0 ? path.replace("%20", " ")
          : path.substring(dependencyIndex).replace("%20", " ");

      return resourceLoader.fetch(Paths.get(workingDir.getPath(), resourceName).toUri().toString().replace("%20", " "));
    }

    return resourceLoader.fetch(path);
  }
}
