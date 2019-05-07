/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import org.junit.Test;
import org.mule.apikit.ApiParser;
import org.mule.apikit.model.api.ApiRef;

import static org.junit.Assert.assertTrue;

public class ValidationReportTestCase {

  @Test
  public void oasValidationReport() {
    final String api = resource("/validation-level-result.json");

    final ApiParser wrapper = new ParserService().getParser(ApiRef.create(api), ParserConfiguration.AUTO);
    assertTrue(wrapper.validate().conforms());
  }

  private static String resource(final String path) {
    return ResourcesUtils.resource(ValidationReportTestCase.class, path);
  }
}
