/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.loader;

import amf.client.remote.Content;
import amf.client.resource.ResourceLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SnifferResourceLoader implements ResourceLoader {
  private HashSet<String> resources = new LinkedHashSet<>();

  public SnifferResourceLoader() {
  }

  public List<String> getResources() {
    return new ArrayList(resources);
  }

  @Override
  public CompletableFuture<Content> fetch(String resource) {
    resources.add(resource);
    return CompletableFuture.supplyAsync(() -> {
      throw new RuntimeException("Failed to apply.");
    });
  }

  @Override
  public boolean accepts(String resource) {
    return true;
  }
}
