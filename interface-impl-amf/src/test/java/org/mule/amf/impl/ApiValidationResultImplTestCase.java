/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import amf.core.client.platform.validation.AMFValidationResult;
import org.junit.Test;
import org.mockito.Mockito;
import org.mule.amf.impl.parser.rule.ApiValidationResultImpl;
import org.mule.apikit.validation.Severity;

public class ApiValidationResultImplTestCase {

  @Test
  public void getWarningSeverity() {
    AMFValidationResult result = Mockito.mock(AMFValidationResult.class);
    Mockito.when(result.severityLevel()).thenReturn("Warning");
    ApiValidationResultImpl apiValidationResult = new ApiValidationResultImpl(result);
    assertThat(apiValidationResult.getSeverity(), equalTo(Severity.WARNING));
  }
}
