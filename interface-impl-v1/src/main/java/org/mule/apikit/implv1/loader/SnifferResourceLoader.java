/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.loader;

import org.raml.parser.loader.ResourceLoader;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import static org.mule.apikit.common.ApiSyncUtils.getApi;
import static org.mule.apikit.common.ApiSyncUtils.getFileName;
import static org.mule.apikit.common.ApiSyncUtils.isSyncProtocol;
import static org.mule.apikit.common.ReferencesUtils.toURI;

public class SnifferResourceLoader implements ResourceLoader {
  private ResourceLoader resourceLoader;
  private String apiSyncRoot;
  private HashSet<String> resources = new LinkedHashSet<>();

  public SnifferResourceLoader(ResourceLoader resourceLoaders, String ramlPath) {
    this.resourceLoader = resourceLoaders;
    this.apiSyncRoot = isSyncProtocol(ramlPath) ? getApi(ramlPath) : null;
  }

  @Nullable
  @Override
  public InputStream fetchResource(String s) {
    InputStream inputStream = resourceLoader.fetchResource(s);
    if (inputStream != null) {
      if (isSyncProtocol(s)) {
        resources.add(Paths.get(getApi(s), getFileName(s)).normalize().toString());
      } else if (apiSyncRoot != null && !isSyncProtocol(s)) {
        resources.add(Paths.get(apiSyncRoot, s).normalize().toString());
      } else {
        resources.add(Paths.get(toURI(s).getPath()).normalize().toUri().toString());
      }
    }
    return inputStream;
  }

  public List<String> getResources() {
    return new ArrayList(resources);
  }

}
