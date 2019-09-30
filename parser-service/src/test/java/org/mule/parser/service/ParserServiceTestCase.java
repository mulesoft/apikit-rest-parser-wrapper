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

import org.mule.amf.impl.model.AMFImpl;
import org.mule.apikit.ApiType;
import org.mule.apikit.implv1.model.RamlImplV1;
import org.mule.apikit.implv2.v10.model.RamlImpl10V2;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.result.ParseResult;
import org.mule.parser.service.result.ParsingIssue;

import java.net.URI;
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

  @Test
  public void getAllReferences(){
    String expected = resource("/example-with-include/schemas/team.raml");
    String api = resource("/example-with-include/example-with-include.raml");

    //AMF
    ParseResult wrapperAMF = new ParserService().parse(ApiReference.create(api), ParserMode.AMF);
    assertNotNull(wrapperAMF);
    assertTrue(wrapperAMF.get() instanceof AMFImpl);
    assertEquals(1,wrapperAMF.get().getAllReferences().size());
    assertEquals(expected.replaceFirst("file:","file://"), wrapperAMF.get().getAllReferences().get(0));

    //RAMLv2
    ParseResult wrapperRAMLv2 = new ParserService().parse(ApiReference.create(api), ParserMode.RAML);
    assertTrue(wrapperRAMLv2.get() instanceof RamlImpl10V2);
    assertNotNull(wrapperRAMLv2);
    assertEquals(1,wrapperRAMLv2.get().getAllReferences().size());
    assertEquals(expected, wrapperRAMLv2.get().getAllReferences().get(0));

    //RAMLv1
    String expectedFor08 = resource("/example-with-include/schemas/atom.xsd");
    String api08 = resource("/example-with-include/api-08-with-include.raml");
    ParseResult wrapperRAML08 = new ParserService().parse(ApiReference.create(api08), ParserMode.RAML);
    assertTrue(wrapperRAML08.get() instanceof RamlImplV1);
    assertNotNull(wrapperRAML08);
    assertEquals(1,wrapperRAML08.get().getAllReferences().size());
    assertEquals(expectedFor08, wrapperRAML08.get().getAllReferences().get(0));

  }

  private static String resource(final String path) {
    return ResourcesUtils.resource(ParserServiceTestCase.class, path);
  }
}
