/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.model.parameter;

import java.util.Set;

public class FileProperties {

  private final Integer minLength;

  private final Integer maxLength;

  private final Set<String> fileTypes;


  public FileProperties(Integer minLength, Integer maxLength,
      Set<String> fileTypes) {
    this.minLength = minLength;
    this.maxLength = maxLength;
    this.fileTypes = fileTypes;
  }

  public Integer getMinLength() {
    return minLength;
  }

  public Integer getMaxLength() {
    return maxLength;
  }

  public Set<String> getFileTypes() {
    return fileTypes;
  }
}
