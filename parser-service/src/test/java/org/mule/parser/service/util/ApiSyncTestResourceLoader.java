/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Enumeration;
import org.mule.apikit.loader.ResourceLoader;

public class ApiSyncTestResourceLoader implements ResourceLoader {

  private final ClassLoader contextClassLoader;

  public ApiSyncTestResourceLoader() {
    contextClassLoader = Thread.currentThread().getContextClassLoader();
  }

  @Override
  public InputStream getResourceAsStream(String name) {
    return contextClassLoader.getResourceAsStream(getResourceName(name));
  }

  public URI getResource(String name) {
    URL resource = contextClassLoader.getResource(getResourceName(name));
    try {
      return resource != null ? resource.toURI() : null;
    } catch (URISyntaxException e) {
      return null;
    }
  }

  private String getResourceName(String name) {
    if (name.startsWith("resource::")) {
      String[] resource = name.split(":");
      name = Paths.get(resource[2], resource[3], resource[4], resource[7]).toString();
    }
    if (name.startsWith("org.custom.api")) {
      name = Paths.get("apis-with-references/api-with-exchange/10/exchange_modules", name).toString();
    }
    return name;
  }
}