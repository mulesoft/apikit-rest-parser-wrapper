/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.loader;

import org.mule.apikit.common.ApiSyncUtils;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;

import javax.annotation.Nullable;
import java.io.InputStream;

import static org.mule.apikit.common.ApiSyncUtils.isExchangeModules;
import static org.mule.apikit.common.ApiSyncUtils.isSyncProtocol;

public class ApiSyncResourceLoader implements ResourceLoader {

  private ResourceLoader resourceLoader;

  private String rootRamlResource;

  public ApiSyncResourceLoader(String rootRaml) {
    this(rootRaml, new DefaultResourceLoader());
  }

  public ApiSyncResourceLoader(String rootRaml, ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
    this.rootRamlResource = getRootRamlResource(rootRaml);
  }

  private String getRootRamlResource(String rootRamlResource) {
    return rootRamlResource.substring(0, rootRamlResource.lastIndexOf(":") + 1);
  }


  @Nullable
  @Override
  public InputStream fetchResource(String s) {
    InputStream stream = null;

    if (s.startsWith("/"))
      s = s.substring(1);

    if (isExchangeModules(s)) {
      stream = getApiSyncResource(s);
    }

    if (stream != null)
      return stream;

    if (isSyncProtocol(s))
      return resourceLoader.fetchResource(s);

    return resourceLoader.fetchResource(rootRamlResource + s);
  }

  private InputStream getApiSyncResource(String s) {
    String apiSyncResource = ApiSyncUtils.toApiSyncResource(s);
    if (apiSyncResource != null)
      return resourceLoader.fetchResource(apiSyncResource);
    return null;
  }
}
