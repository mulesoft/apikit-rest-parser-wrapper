/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.metadata.TypedValue;

public class ValidRequest {

  private final TypedValue<Object> payload;
  private final HttpRequestAttributes attributes;

  public ValidRequest(HttpRequestAttributes attributes, TypedValue<Object> payload) {
    this.attributes = attributes;
    this.payload = payload;
  }

  public TypedValue<Object> getPayload() {
    return payload;
  }

  public HttpRequestAttributes getAttributes() {
    return attributes;
  }
}
