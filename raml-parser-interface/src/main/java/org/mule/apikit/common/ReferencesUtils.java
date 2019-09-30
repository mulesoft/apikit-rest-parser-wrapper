/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.common;

import java.io.File;
import java.net.URI;

public class ReferencesUtils {
  private ReferencesUtils() {}

  public static URI toURI(String path) {
    return URI.create(path.replaceAll("\\s+","%20").replaceAll(File.separator, "/"));
  }
}
