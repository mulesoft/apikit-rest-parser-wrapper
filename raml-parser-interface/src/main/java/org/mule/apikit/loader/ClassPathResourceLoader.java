/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.loader;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ClassPathResourceLoader implements ResourceLoader {

  @Override
  public URI getResource(String path) {
    try {
      // The replacement is necessary because MuleApplicationClassLoader fails if the path is percent encoded.
      final URL resource = Thread.currentThread().getContextClassLoader().getResource(path.replaceAll("%20", " "));
      return resource != null ? resource.toURI() : null;
    } catch (URISyntaxException e) {
      return null;
    }
  }

}
