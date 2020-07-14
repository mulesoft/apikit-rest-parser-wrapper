/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.apikit.implv2.v10.parser.rule;

import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.implv2.ParserWrapperV2;
import org.mule.apikit.implv2.parser.rule.ApiValidationResultImpl;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ApiValidationResultImplTest {

  private static final String DOES_NOT_EXIST = "does not exist";
  private static final String NO_VALID_RESOURCE_PATH = "not-valid-resource";

  private ApiValidationResultImpl noValidPathResult;
  private ApiValidationResultImpl nullPathResult;

  @Before
  public void setUp() {
    ParserWrapperV2 parserWrapper = new ParserWrapperV2(NO_VALID_RESOURCE_PATH, Collections.emptyList());
    noValidPathResult = (ApiValidationResultImpl) parserWrapper.validate().getResults().iterator().next();
    parserWrapper = new ParserWrapperV2(null, Collections.emptyList());
    nullPathResult = (ApiValidationResultImpl) parserWrapper.validate().getResults().iterator().next();
  }

  @Test
  public void getMessageTest() {
    assertTrue(noValidPathResult.getMessage().contains(DOES_NOT_EXIST));
    assertTrue(nullPathResult.getMessage().contains(DOES_NOT_EXIST));
  }

  @Test
  public void getLineTest() {
    assertEquals(Optional.empty(), noValidPathResult.getLine());
    assertEquals(Optional.empty(), nullPathResult.getLine());
  }

  @Test
  public void getPathTest() {
    assertNull(noValidPathResult.getPath());
    assertNull(nullPathResult.getPath());
  }

  @Test
  public void getSeverityTest() {
    assertEquals("ERROR", noValidPathResult.getSeverity().name());
    assertEquals("ERROR", nullPathResult.getSeverity().name());
  }

  @Test
  public void toStringTest() {
    assertTrue(noValidPathResult.toString().contains(DOES_NOT_EXIST));
    assertTrue(nullPathResult.toString().contains(DOES_NOT_EXIST));
  }
}
