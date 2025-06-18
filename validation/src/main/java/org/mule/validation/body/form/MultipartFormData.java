/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.body.form;

import org.mule.validation.exception.InvalidFormParameterException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;

public class MultipartFormData {

  private final HttpEntity multipartFormEntity;

  public MultipartFormData(HttpEntity multipartFormEntity) {
    this.multipartFormEntity = multipartFormEntity;
  }

  //TODO: Enhance performance using piped streams
  public InputStream getInputStream() throws InvalidFormParameterException {
    try {
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      this.multipartFormEntity.writeTo(outStream);
      outStream.flush();
      return new ByteArrayInputStream(outStream.toByteArray());
    } catch (IOException e) {
      throw new InvalidFormParameterException(e.getMessage());
    }
  }

  public long getLength() {
    return this.multipartFormEntity.getContentLength();
  }

  public String getContentType() {
    return this.multipartFormEntity.getContentType().getValue();
  }
}
