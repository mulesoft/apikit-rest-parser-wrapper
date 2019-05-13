/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.loader;

import static org.mule.apikit.common.ApiSyncUtils.isExchangeModules;
import static org.mule.apikit.common.ApiSyncUtils.isSyncProtocol;
import static org.mule.apikit.common.ApiSyncUtils.toApiSyncResource;

import java.net.URI;

public class ApiSyncResourceLoader implements ResourceLoader {

  private ResourceLoader resourceLoader;
  private String rootResource;

  public ApiSyncResourceLoader(String resource) {
    this(resource, new ClassPathResourceLoader());
  }

  public ApiSyncResourceLoader(String resource, ResourceLoader resourceLoader) {
    this.rootResource = getRootRamlResource(resource);
    this.resourceLoader = resourceLoader;
  }

  @Override
  public URI getResource(String path) {
    final String resourcePath;
    if (path.startsWith("/"))
      resourcePath = path.substring(1);
    else
      resourcePath = path;

    if (isExchangeModules(resourcePath)) {
      return resourceLoader.getResource(toApiSyncResource(resourcePath));
    }
    else if (isSyncProtocol(path)) {
      return resourceLoader.getResource(resourcePath);
    }
    else {
      return resourceLoader.getResource(rootResource + resourcePath);
    }
  }

  private String getRootRamlResource(String rootRamlResource) {
    return rootRamlResource.substring(0, rootRamlResource.lastIndexOf(":") + 1);
  }
}
