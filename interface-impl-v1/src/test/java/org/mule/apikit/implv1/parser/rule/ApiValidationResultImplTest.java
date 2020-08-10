/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.apikit.implv1.parser.rule;

import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.common.LazyValue;
import org.mule.apikit.implv1.ParserWrapperV1;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ApiValidationResultImplTest {

  private ApiValidationResultImpl result;

  @Before
  public void setUp() {
    ParserWrapperV1 parserWrapper = new ParserWrapperV1("not-valid-resource", new LazyValue<>(Collections::emptyList));
    result = (ApiValidationResultImpl) parserWrapper.validate().getResults().iterator().next();
  }

  @Test
  public void getMessageTest() {
    assertEquals("RAML resource not found", result.getMessage());
  }

  @Test
  public void getLineTest() {
    assertEquals(-1, result.getLine().get().intValue());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getPathTest() {
    result.getPath();
  }

  @Test
  public void getSeverityTest() {
    assertEquals("ERROR", result.getSeverity().name());
  }

  @Test
  public void toStringTest() {
    assertTrue(result.toString().contains("ERROR"));
  }
}
