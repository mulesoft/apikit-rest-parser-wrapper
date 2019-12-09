/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.helpers;

import static org.mule.validation.attributes.HeaderName.CONTENT_TYPE;
import static java.util.Collections.emptyList;
import static org.mule.runtime.api.metadata.MediaType.parse;

import org.mule.validation.attributes.HeaderName;
import com.google.common.base.Strings;
import java.util.List;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.util.MultiMap;

public class AttributesHelper {

  private static final String ANY_RESPONSE_MEDIA_TYPE = "*/*";

  private AttributesHelper() {}

  public static List<String> getParamsIgnoreCase(MultiMap<String, String> parameters, String name) {
    return parameters.keySet().stream()
        .filter(header -> header.equalsIgnoreCase(name))
        .findFirst().map(parameters::getAll)
        .orElse(emptyList());
  }

  public static String getAcceptedResponseMediaTypes(MultiMap<String, String> headers) {
    String acceptableResponseMediaTypes = getParamIgnoreCase(headers, "accept");
    if (Strings.isNullOrEmpty(acceptableResponseMediaTypes)) {
      return ANY_RESPONSE_MEDIA_TYPE;
    }
    return acceptableResponseMediaTypes;
  }

  public static String getParamIgnoreCase(MultiMap<String, String> parameters, String name) {
    for (String header : parameters.keySet()) {
      if (header.equalsIgnoreCase(name.toLowerCase())) {
        return parameters.get(header);
      }
    }
    return null;
  }


  public static String getHeaderIgnoreCase(HttpRequestAttributes attributes, HeaderName name) {
    return getHeaderIgnoreCase(attributes, name.getName());
  }

  public static String getHeaderIgnoreCase(HttpRequestAttributes attributes, String name) {
    final MultiMap<String, String> headers = attributes.getHeaders();
    return getParamIgnoreCase(headers, name);
  }

  public static String getMediaType(String mediaType) {
    MediaType mType = parse(mediaType);
    return String.format("%s/%s", mType.getPrimaryType(), mType.getSubType());
  }

  public static String getMediaType(HttpRequestAttributes attributes) {
    final String contentType = getHeaderIgnoreCase(attributes, CONTENT_TYPE);
    if (contentType != null) {
      return AttributesHelper.getMediaType(contentType);
    }
    return null;
  }

}
