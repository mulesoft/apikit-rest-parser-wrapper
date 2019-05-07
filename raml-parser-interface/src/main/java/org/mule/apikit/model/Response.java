/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.model;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.mule.apikit.model.parameter.Parameter;

import java.util.Map;

public interface Response {

  Map<String, MimeType> getBody();

  boolean hasBody();

  Map<String, Parameter> getHeaders();

  void setBody(Map<String, MimeType> body);

  void setHeaders(Map<String, Parameter> headers);

  Object getInstance();

  // We can implement if we need cache
  default Map<String, String> getExamples() {
    final Map<String, String> result = new HashMap<>();
    if (hasBody()) {
      final Map<String, MimeType> map = getBody();
      for (Map.Entry<String, MimeType> entry : map.entrySet()) {
        final MimeType mimeType = entry.getValue();
        final String contentType = mimeType.getType();
        final String example = mimeType.getExample();
        if (StringUtils.isNotEmpty(example))
          result.put(contentType, example);
      }
    }
    return result;
  }
}
