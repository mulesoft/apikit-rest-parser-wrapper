/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.validation;

import static org.mule.apikit.validation.Severity.ERROR;

import java.util.Optional;

public class ExceptionApiValidationResult implements ApiValidationResult {

  private final Exception exception;

  public ExceptionApiValidationResult(Exception e) {
    this.exception = e;
  }

  @Override
  public String getMessage() {
    return exception.getMessage();
  }

  @Override
  public Optional<Integer> getLine() {
    return Optional.empty();
  }

  @Override
  public String getPath() {
    return "";
  }

  @Override
  public Severity getSeverity() {
    return ERROR;
  }
}
