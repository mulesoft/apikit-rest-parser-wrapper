/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Enumeration;

public class ApiSyncTestClassLoader extends URLClassLoader {
  public ApiSyncTestClassLoader() {
    super(new URL[] {}, Thread.currentThread().getContextClassLoader());
  }

  @Override
  public InputStream getResourceAsStream(String name) {
    return super.getResourceAsStream(getResourceName(name));
  }

  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    return super.getResources(getResourceName(name));
  }

  @Override
  public URL getResource(String name) {
    return super.getResource(getResourceName(name));
  }

  private String getResourceName(String name) {
    if (name.startsWith("resource::")) {
      String[] resource = name.split(":");
      name = Paths.get(resource[2], resource[3], resource[4], resource[7]).toString();
    }
    return name;
  }
}