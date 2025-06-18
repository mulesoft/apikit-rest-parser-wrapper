/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.helpers;

import java.nio.charset.Charset;
import java.util.Optional;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;

public class CharsetUtils {

  private CharsetUtils() {}

  public static String getCharset(MultiMap<String, String> headers, Object payload) {
    String charset = getHeaderCharset(headers);
    if (charset == null) {
      if (payload instanceof TypedValue) {
        return normalizeCharset(getEncoding((TypedValue) payload));
      }
    }
    return normalizeCharset(charset);
  }

  private static String getHeaderCharset(MultiMap<String, String> headers) {
    return getCharset(AttributesHelper.getParamIgnoreCase(headers, "Content-Type"));
  }

  public static String getCharset(String contentType) {
    if (contentType == null) {
      return null;
    }
    MediaType mediaType = MediaType.parse(contentType);
    Optional<Charset> charset = mediaType.getCharset();
    return charset.map(Charset::name).orElse(null);
  }

  private static String normalizeCharset(String encoding) {
    if (encoding != null && encoding.matches("(?i)UTF-16.+")) {
      return "UTF-16";
    }
    return encoding;
  }

  private static <T> String getEncoding(TypedValue<T> typedValue) {
    return typedValue.getDataType().getMediaType().getCharset().orElse(Charset.defaultCharset()).toString();
  }

}
