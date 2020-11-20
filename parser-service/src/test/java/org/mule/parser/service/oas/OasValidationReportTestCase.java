/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.oas;

import org.junit.Test;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserMode;
import org.mule.parser.service.internal.ParserService;
import org.mule.parser.service.ResourcesUtils;
import org.mule.parser.service.result.internal.ParseResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OasValidationReportTestCase {

  @Test
  public void oasValidationReport() {
    String api = resource("/oas/validation-level-result.json");
    ParseResult wrapper = new ParserService().parse(ApiReference.create(api), ParserMode.AUTO);
    assertTrue(wrapper.success());
  }

  @Test
  public void oasValidationReportForUnsupportedFeatures() {
    String apiLocation = resource("/oas/callback-and-link-example.yaml");
    ParseResult wrapper = new ParserService().parse(ApiReference.create(apiLocation), ParserMode.AUTO);
    assertTrue(wrapper.success());
    assertEquals(2, wrapper.getWarnings().size());
    assertTrue(wrapper.getWarnings().get(0).cause().contains("Callbacks are not supported yet"));
    assertTrue(wrapper.getWarnings().get(1).cause().contains("Links are not supported yet"));
  }


  private static String resource(final String path) {
    return ResourcesUtils.resource(OasValidationReportTestCase.class, path);
  }
}
