/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.loader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * Represents a way of getting resources from the application
 */
public interface ResourceLoader {

  /**
   * Gets the root RAML File
   *
   * @param relativePath Location of the root RAML file relative to the /mule/resources/api folder or a resource:: in case when
   *        the API is defined as a dependency (API sync)
   * @return {@link URI} to the RAML resource
   */
  URI getResource(String relativePath);

  default InputStream getResourceAsStream(String relativePath) {
    URI uri = getResource(relativePath);
    if (uri == null) {
      return null;
    }
    try {
      URL url = uri.toURL();
      URLConnection urlConnection = url.openConnection();
      urlConnection.setUseCaches(false);
      return urlConnection.getInputStream();
    } catch (IOException e) {
      return null;
    }
  }

}
