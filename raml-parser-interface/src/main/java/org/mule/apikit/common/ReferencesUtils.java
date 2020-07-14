/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.common;

import java.net.URI;

public class ReferencesUtils {

  private static String OS = null;

  private ReferencesUtils() {}

  public static URI toURI(String path) {
    URI uri;
    if (isWindows()) {
      uri = URI.create(path.replaceAll("\\s+", "%20").replaceAll("\\\\", "/"));
    } else {
      uri = URI.create(path.replaceAll("\\s+", "%20"));
    }
    return uri;
  }

  public static String getOsName() {
    if (OS == null) {
      OS = System.getProperty("os.name");
    }
    return OS;
  }

  private static boolean isWindows() {
    return getOsName().startsWith("Windows");
  }

}
