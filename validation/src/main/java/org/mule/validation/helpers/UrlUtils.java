/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.helpers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.validation.uri.URICoder;

public class UrlUtils {

  private static final Set<Character> ESCAPE_CHARS = new HashSet<Character>(
      Arrays.asList('/', '{', '}'));

  private UrlUtils() {}

  // TODO : clean this methods
  public static String getRequestPath(HttpRequestAttributes attributes) {
    // raw request path is encoded only when it's not encoded
    // if raw request path is decoded always, and encoded again
    // we can get "not found" error, when raw request path contains %2F (%2F decode-> "/")
    // example raw request path : "uri-param/AA%2F11%2F00000070" decode-> "org.mule.validation.uri-param/AA/11/00000070" encode->"org.mule.validation.uri-param/AA/11/00000070"
    boolean isEncoded = !attributes.getRequestPath().equals(attributes.getRawRequestPath());
    String rawRequestPath = isEncoded ? attributes.getRawRequestPath() : encode(attributes.getRawRequestPath());
    String path = getRelativePath(attributes.getListenerPath(), rawRequestPath);
    return path.isEmpty() ? "/" : path;
  }

  private static String encode(String url) {
    return URICoder.encode(url, ESCAPE_CHARS);
  }

  public static String getRelativePath(String baseAndApiPath, String requestPath) {
    int character = getEndOfBasePathIndex(baseAndApiPath, requestPath);
    String relativePath = requestPath.substring(character);
    if (!"".equals(relativePath)) {
      for (; character > 0 && Character.compare(requestPath.charAt(character - 1), '/') == 0; character--) {
        relativePath = "/" + relativePath;
      }
    } else {
      relativePath += "/";
    }

    return relativePath;
  }

  private static int getEndOfBasePathIndex(String baseAndApiPath, String requestPath) {
    int index = baseAndApiPath.lastIndexOf('/') + 1;
    if (index > requestPath.length()) {
      return requestPath.length();
    }
    return index;
  }

}
