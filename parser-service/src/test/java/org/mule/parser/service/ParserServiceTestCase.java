/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;
import static org.mule.apikit.model.ApiVendor.OAS_20;
import static org.mule.apikit.model.ApiVendor.RAML_08;
import static org.mule.apikit.model.ApiVendor.RAML_10;

import org.mule.apikit.ApiType;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.result.ParseResult;
import org.mule.parser.service.result.ParsingIssue;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParserServiceTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void raml08Wrapper() {
    String api = resource("/api-08.raml");

    ParserService parserService = new ParserService();
    ParseResult wrapper = parserService.parse(ApiReference.create(api), ParserMode.RAML);
    assertNotNull(wrapper);
    assertThat(wrapper.get().getType(), is(ApiType.RAML));
    assertThat(wrapper.get().getApiVendor(), is(RAML_08));
    assertThat(wrapper.success(), is(true));
    assertThat(wrapper.getErrors().size(), is(0));
  }

  @Test
  public void raml10Wrapper() {
    String api = resource("/api-10.raml");

    ParseResult wrapper = new ParserService().parse(ApiReference.create(api), ParserMode.RAML);
    assertNotNull(wrapper);
    assertThat(wrapper.get().getType(), is(ApiType.RAML));
    assertThat(wrapper.get().getApiVendor(), is(RAML_10));
  }

  @Test
  public void raml10AmfWrapper() {
    String api = resource("/example-with-include/example-with-include.raml");

    ParseResult wrapper = new ParserService().parse(ApiReference.create(api), ParserMode.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.get().getType(), is(ApiType.AMF));
    assertThat(wrapper.get().getApiVendor(), is(RAML_10));
  }

  @Test
  public void oasJson20Wrapper() {
    String api = resource("/petstore.json");

    ParseResult wrapper = new ParserService().parse(ApiReference.create(api), ParserMode.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.get().getType(), is(ApiType.AMF));
    assertThat(wrapper.get().getApiVendor(), is(OAS_20));
  }

  @Test
  public void oasYaml20Wrapper() {
    String api = resource("/petstore.yaml");

    ParseResult wrapper = new ParserService().parse(ApiReference.create(api), ParserMode.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.get().getType(), is(ApiType.AMF));
    assertThat(wrapper.get().getApiVendor(), is(OAS_20));
  }

  @Test
  public void fallbackParser() {
    ParserService parserService = new ParserService();
    ParseResult wrapper = parserService.parse(ApiReference.create(resource("/api-with-fallback-parser.raml")));

    assertNotNull(wrapper);
    List<ParsingIssue> warnings = wrapper.getWarnings();
    assertThat(warnings.size(), is(1));
    assertThat(warnings.get(0).cause(), containsString("AMF parsing failed, fallback into RAML parser"));
  }

  @Test
  public void invalidRAML() {
    ParserService parserService = new ParserService();
    ParseResult result = parserService.parse(ApiReference.create(resource("/with-invalid-errors.raml")));
    List<ParsingIssue> errors = result.getErrors();
    List<ParsingIssue> warnings = result.getWarnings();

    assertThat(warnings.size(), is(1));
    assertThat(warnings.get(0).cause(), containsString("AMF parsing failed, fallback into RAML parser"));
    assertThat(errors.size(), is(1));
    assertThat(errors.get(0).cause(), containsString("Invalid element resource for /pet"));
  }

  private static String resource(final String path) {
    return ResourcesUtils.resource(ParserServiceTestCase.class, path);
  }
}
