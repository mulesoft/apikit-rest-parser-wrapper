/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.parser.rule;

import org.mule.apikit.validation.ApiValidationResult;
import org.mule.apikit.validation.Severity;

import static org.mule.apikit.validation.Severity.WARNING;
import static org.raml.parser.rule.ValidationResult.Level.WARN;

import java.util.Optional;

import org.raml.parser.rule.ValidationResult;

public class ApiValidationResultImpl implements ApiValidationResult {

  private ValidationResult validationResult;

  public ApiValidationResultImpl(ValidationResult validationResult) {
    this.validationResult = validationResult;
  }

  @Override
  public String getMessage() {
    return validationResult.getMessage();
  }

  @Override
  public Optional<Integer> getLine() {
    return Optional.of(validationResult.getLine());
  }

  @Override
  public String getPath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Severity getSeverity() {
    if (validationResult.getLevel().equals(WARN)) {
      return WARNING;
    }
    return Severity.fromString(validationResult.getLevel().name());
  }
}
