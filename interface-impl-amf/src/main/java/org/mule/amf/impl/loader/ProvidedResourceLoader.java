/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.loader;

import amf.core.client.common.remote.Content;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.io.IOUtils;
import org.mule.apikit.loader.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

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

    try {
      InputStream streamResource = getResourceFromURI(resourceName);

      if (streamResource != null) {
        future.complete(new Content(getContentFromStream(streamResource), resourceName));
        return future;
      }

      URI resourceUri = resourceLoader.getResource(resourceName);
      if (resourceUri != null) {
        streamResource = resourceLoader.getResourceAsStream(resourceName);
        future.complete(new Content(getContentFromStream(streamResource), resourceUri.toString()));
        return future;
      }
      future.completeExceptionally(new Exception("Failed to fetch resource '" + resourceName + "'"));
      return future;

    } catch (Exception e) {
      future.completeExceptionally(new RuntimeException("Failed to fetch resource '" + resourceName + "'", e));
      return future;
    }
  }

  private String getContentFromStream(InputStream stream) {
    try {
      return IOUtils.toString(stream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isValidUri(String resourceName) {
    try {
      new URI(resourceName);
      return true;
    } catch (URISyntaxException e) {
      return false;
    }
  }

  private InputStream getResourceFromURI(String resourceName) {
    try {
      if (!isValidUri(resourceName)) {
        return null;
      }
      URL url = new URI(resourceName).toURL();
      URLConnection urlConnection = url.openConnection();
      urlConnection.setUseCaches(false);
      return urlConnection.getInputStream();
    } catch (Exception e) {
      return null;
    }
  }
}

