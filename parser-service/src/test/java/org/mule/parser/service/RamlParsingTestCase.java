/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import static java.util.Arrays.asList;
import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.StringContains.containsString;
import static org.mule.parser.service.ParserMode.AMF;
import static org.mule.parser.service.ParserMode.RAML;

import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.result.ParseResult;

import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RamlParsingTestCase {

  private ParserService service = new ParserService();

  @Parameter(0)
  public ParserMode mode;

  @Parameter(1)
  public String versionNumber;

  @Parameters(name = "Parser {0} - RAML Version {1}")
  public static Iterable<Object[]> data() {
    return asList(new Object[][] {
      {RAML, "08"},
      {RAML, "10"},
      {AMF, "10"}
    });
  }

  @Test
  public void parseFileWithSpacesInName() {
    ParseResult result = service.parse(ApiReference.create("api-with-spaces/" + versionNumber + "/api spaces.raml"), mode);
    if (!result.success()) {
      fail(result.getErrors().stream().map(Object::toString).collect(Collectors.joining("\n")));
    }
    assertThat(result.get().getLocation(), containsString("api spaces.raml"));
    assertThat(result.get().getAllReferences(), hasSize(0));
  }

}
