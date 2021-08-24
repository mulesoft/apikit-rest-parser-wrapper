/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.loader;

import amf.core.client.common.remote.Content;
import org.mule.apikit.loader.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.IOUtils;

public class ProvidedResourceLoader implements amf.core.client.platform.resource.ResourceLoader {

  private ResourceLoader resourceLoader;

  public ProvidedResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public CompletableFuture<Content> fetch(String resourceName) {
    final CompletableFuture<Content> future = new CompletableFuture<>();

    if (resourceName == null || resourceName.isEmpty()) {
      throw new RuntimeException("Failed to apply.");
    }

    final URI resource = resourceLoader.getResource(resourceName);

    if (resource != null) {
      final InputStream stream = resourceLoader.getResourceAsStream(resourceName);
      try {
        final String resourceAsString = IOUtils.toString(stream);
        final Content content = new Content(resourceAsString, resourceName);
        future.complete(content);
      } catch (IOException e) {
        future.completeExceptionally(new RuntimeException("Failed to fetch resource '" + resourceName + "'"));
      }
    } else {
      future.completeExceptionally(new Exception("Failed to fetch resource '" + resourceName + "'"));
    }

    return future;
  }
}

