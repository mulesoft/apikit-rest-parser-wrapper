/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.uri;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UriUtils {

  private static final Set<Character> ESCAPE_CHARS = new HashSet<>(
      Arrays.asList('/', '{', '}'));

  public static String encode(String url) {
    return URICoder.encode(url, ESCAPE_CHARS);
  }


}
