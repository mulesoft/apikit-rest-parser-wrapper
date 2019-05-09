/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.parser.rule;

import org.mule.apikit.validation.ApiValidationResult;
import org.mule.apikit.validation.Severity;

import java.util.Optional;

public class ApiValidationResultImpl implements ApiValidationResult {

  private final org.raml.v2.api.model.common.ValidationResult validationResult;

  public ApiValidationResultImpl(org.raml.v2.api.model.common.ValidationResult validationResult) {
    this.validationResult = validationResult;
  }

  @Override
  public String getMessage() {
    return validationResult.getMessage();
  }

  @Override
  public String getPath() {
    return validationResult.getPath();
  }

  @Override
  public Optional<Integer> getLine() {
    return Optional.empty();
  }

  @Override
  public Severity getSeverity() {
    return Severity.ERROR;
  }

  @Override
  public String toString() {
    return validationResult.toString();
  }
}
