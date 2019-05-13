/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import static org.junit.Assert.assertTrue;

import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.result.ParseResult;

import org.junit.Test;

public class ValidationReportTestCase {

  @Test
  public void oasValidationReport() {
    String api = resource("/validation-level-result.json");
    ParseResult wrapper = new ParserService().parse(ApiReference.create(api), ParserMode.AUTO);
    assertTrue(wrapper.success());
  }

  private static String resource(final String path) {
    return ResourcesUtils.resource(ValidationReportTestCase.class, path);
  }
}
