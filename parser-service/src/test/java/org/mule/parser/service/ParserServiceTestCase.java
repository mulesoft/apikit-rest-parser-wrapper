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
import org.mule.apikit.ApiType;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.result.ParseResult;
import org.mule.parser.service.result.ParsingIssue;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mule.apikit.model.ApiVendor.OAS_20;
import static org.mule.apikit.model.ApiVendor.OAS_30;
import static org.mule.apikit.model.ApiVendor.RAML_08;
import static org.mule.apikit.model.ApiVendor.RAML_10;

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
    String api = resource("/oas/petstore.json");

    ParseResult wrapper = new ParserService().parse(ApiReference.create(api), ParserMode.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.get().getType(), is(ApiType.AMF));
    assertThat(wrapper.get().getApiVendor(), is(OAS_20));
  }

  @Test
  public void oasYaml20Wrapper() {
    String api = resource("/oas/petstore.yaml");

    ParseResult wrapper = new ParserService().parse(ApiReference.create(api), ParserMode.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.get().getType(), is(ApiType.AMF));
    assertThat(wrapper.get().getApiVendor(), is(OAS_20));
  }
  // === PARSER AUTO ===

  @Test
  public void fallbackParsingValidRAML08WithAMFParser() {
    ParserService parserService = new ParserService();
    ParseResult wrapper = parserService.parse(ApiReference.create(resource("/api-08.raml")));

    assertNotNull(wrapper);
    assertNotNull(wrapper.get());
    assertThat(wrapper.success(), is(true));
    assertThat(wrapper.getErrors().size(), is(0));
  }

  @Test
  public void oas3Yaml20Wrapper() {
    String api = resource("/oas/oas3.yaml");

    ParseResult wrapper = new ParserService().parse(ApiReference.create(api), ParserMode.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.get().getType(), is(ApiType.AMF));
    assertThat(wrapper.get().getApiVendor(), is(OAS_30));
  }

  @Test
  public void fallbackParsingValidRAML10WithAMFParser() {
    ParserService parserService = new ParserService();
    ParseResult wrapper = parserService.parse(ApiReference.create(resource("/api-10.raml")));

    assertNotNull(wrapper);
    assertNotNull(wrapper.get());
    assertThat(wrapper.success(), is(true));
    assertThat(wrapper.getErrors().size(), is(0));
  }


  @Test
  public void fallbackParsingAMFErrorRAMLOk() {
    ParserService parserService = new ParserService();
    ParseResult wrapper = parserService.parse(ApiReference.create(resource("/api-with-fallback-parser.raml")));

    assertNotNull(wrapper);
    List<ParsingIssue> warnings = wrapper.getWarnings();
    assertThat(warnings.size(), is(2));
    assertThat(warnings.get(0).cause(), containsString("AMF parsing failed, fallback into RAML parser"));
    assertThat(warnings.get(1).cause(), containsString("AMF: expected type: String, found: Integer"));
    assertThat(wrapper.success(), is(true));
    assertThat(wrapper.getErrors().size(), is(0));
  }

  @Test
  public void fallbackParsingOASErrorRAMLError() {
    ParserService parserService = new ParserService();
    ParseResult result = parserService.parse(ApiReference.create(resource("/with-invalid-errors.raml")));
    List<ParsingIssue> errors = result.getErrors();
    List<ParsingIssue> warnings = result.getWarnings();

    assertThat(warnings.size(), is(1));
    assertThat(warnings.get(0).cause(), containsString("AMF parsing failed, fallback into RAML parser"));
    assertThat(errors.size(), is(2));
    assertThat(errors.get(0).cause(), containsString("AMF: YAML map expected"));
    assertThat(errors.get(1).cause(), containsString("Invalid element resource for /pet"));
  }

  @Test
  public void fallbackParsingOASResultOK() {
    String api = resource("/oas/petstore.json");

    ParserService parserService = new ParserService();
    ParseResult wrapper = parserService.parse(ApiReference.create(api));
    assertNotNull(wrapper);
    assertThat(wrapper.success(), is(true));
    assertThat(wrapper.getErrors().size(), is(0));

  }

  @Test
  public void fallbackParsingOASError() {
    String api = resource("/oas/with-invalid-url.json");

    ParserService parserService = new ParserService();
    ParseResult wrapper = parserService.parse(ApiReference.create(api));
    assertNotNull(wrapper);
    assertThat(wrapper.getErrors().size(), is(1));
  }

  // ==================

  private static String resource(final String path) {
    return ResourcesUtils.resource(ParserServiceTestCase.class, path);
  }
}
