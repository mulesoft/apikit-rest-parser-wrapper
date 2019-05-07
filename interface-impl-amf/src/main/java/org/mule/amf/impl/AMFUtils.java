/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import org.mule.apikit.model.api.ApiRef;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class AMFUtils {

  public static URI getPathAsUri(ApiRef apiRef) {
    try {
      final URI uri = new URI(apiRef.getLocation());
      return uri.isAbsolute() ? uri : getUriFromFile(apiRef);
    } catch (URISyntaxException e) {
      return getUriFromFile(apiRef);
    }
  }

  private static URI getUriFromFile(ApiRef apiRef) {
    final String location = apiRef.getLocation();
    if (apiRef.getResourceLoader().isPresent()) {
      final URI uri = apiRef.getResourceLoader().map(loader -> loader.getResource(location)).orElse(null);
      if (uri != null)
        return uri;
    }

    final File file = new File(location);
    if (file.exists())
      return file.toURI();

    final URL resource = Thread.currentThread().getContextClassLoader().getResource(location);

    if (resource != null) {
      try {
        return resource.toURI();
      } catch (URISyntaxException e1) {
        throw new RuntimeException("Couldn't load api in location: " + location);
      }
    } else
      throw new RuntimeException("Couldn't load api in location: " + location);
  }
}
