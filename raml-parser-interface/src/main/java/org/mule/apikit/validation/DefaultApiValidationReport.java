/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.validation;

import java.util.List;

import static org.mule.apikit.validation.Severity.ERROR;

public class DefaultApiValidationReport implements ApiValidationReport {

  private List<ApiValidationResult> validationResults;

  public DefaultApiValidationReport(List<ApiValidationResult> validationResults) {
    this.validationResults = validationResults;
  }

  @Override
  public boolean conforms() {
    return getResults().stream().noneMatch(r -> r.getSeverity().equals(ERROR));
  }

  @Override
  public List<ApiValidationResult> getResults() {
    return validationResults;
  }
}
