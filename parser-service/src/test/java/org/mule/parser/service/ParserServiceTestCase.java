/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mule.apikit.ParserType;
import org.mule.apikit.ApiParser;
import org.mule.apikit.model.api.ApiRef;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mule.apikit.model.ApiVendor.OAS_20;
import static org.mule.apikit.model.ApiVendor.RAML_08;
import static org.mule.apikit.model.ApiVendor.RAML_10;

public class ParserServiceTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void raml08Wrapper() {
    String api = resource("/api-08.raml");

    ParserService parserService = new ParserService();
    ApiParser wrapper = parserService.getParser(ApiRef.create(api), ParserConfiguration.RAML);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.RAML));
    assertThat(wrapper.getApiVendor(), is(RAML_08));
    assertThat(parserService.getParsingErrors().size(), is(0));
  }

  @Test
  public void raml10Wrapper() {
    String api = resource("/api-10.raml");

    ApiParser wrapper = new ParserService().getParser(ApiRef.create(api), ParserConfiguration.RAML);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.RAML));
    assertThat(wrapper.getApiVendor(), is(RAML_10));
  }

  @Test
  public void raml10AmfWrapper() {
    String api = resource("/example-with-include/example-with-include.raml");

    ApiParser wrapper = new ParserService().getParser(ApiRef.create(api), ParserConfiguration.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.AMF));
    assertThat(wrapper.getApiVendor(), is(RAML_10));
  }

  @Test
  public void oasJson20Wrapper() {
    String api = resource("/petstore.json");

    ApiParser wrapper = new ParserService().getParser(ApiRef.create(api), ParserConfiguration.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.AMF));
    assertThat(wrapper.getApiVendor(), is(OAS_20));
  }

  @Test
  public void oasYaml20Wrapper() {
    String api = resource("/petstore.yaml");

    ApiParser wrapper = new ParserService().getParser(ApiRef.create(api), ParserConfiguration.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.AMF));
    assertThat(wrapper.getApiVendor(), is(OAS_20));
  }

  @Test
  public void fallbackParser() {
    ParserService parserService = new ParserService();
    ApiParser wrapper = parserService.getParser(ApiRef.create(resource("/api-with-fallback-parser.raml")));

    assertNotNull(wrapper);
    List<ParsingError> errors = parserService.getParsingErrors();
    assertThat(errors.size(), is(1));
    assertThat(errors.get(0).cause(), containsString("Validation failed using parser: AMF, in file:"));
  }

  @Test
  public void invalidRAML() {
    ParserService parserService = new ParserService();
    try {
      parserService.getParser(ApiRef.create(resource("/with-invalid-errors.raml")));
      fail("Expecting to fail");
    } catch (ParserServiceException e) {
      List<ParsingError> errors = parserService.getParsingErrors();
      assertThat(errors.size(), is(2));
      assertThat(errors.get(0).cause(), containsString("Validation failed using parser: AMF, in file:"));
      assertThat(errors.get(1).cause(), containsString("Validation failed using fallback parser: RAML, in file:"));
    }
  }

  private static String resource(final String path) {
    return ResourcesUtils.resource(ParserServiceTestCase.class, path);
  }
}
