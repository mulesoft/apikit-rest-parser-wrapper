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

import org.mule.amf.impl.ParserWrapperAmf;
import org.mule.raml.implv1.ParserWrapperV1;
import org.mule.raml.implv2.ParserWrapperV2;
import org.mule.raml.interfaces.ParserType;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.model.api.ApiRef;

import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mule.raml.interfaces.model.ApiVendor.OAS_20;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_08;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_10;

public class ParserServiceTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void raml08Wrapper() throws URISyntaxException {

    final String api = resource("/api-08.raml");

    ParserService parserService = new ParserService();
    final ParserWrapper wrapper = parserService.getParser(ApiRef.create(api), ParserType.RAML);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.RAML));
    assertThat(wrapper.getApiVendor(), is(RAML_08));
    assertThat(parserService.getParsingErrors().size(), is(0));
  }

  @Test
  public void raml10Wrapper() throws URISyntaxException {

    final String api = resource("/api-10.raml");

    final ParserWrapper wrapper = new ParserService().getParser(ApiRef.create(api), ParserType.RAML);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.RAML));
    assertThat(wrapper.getApiVendor(), is(RAML_10));
  }

  @Test
  public void raml10AmfWrapper() throws URISyntaxException {

    final String api = resource("/example-with-include/example-with-include.raml");

    final ParserWrapper wrapper = new ParserService().getParser(ApiRef.create(api), ParserType.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.AMF));
    assertThat(wrapper.getApiVendor(), is(RAML_10));
  }

  @Test
  public void oasJson20Wrapper() throws URISyntaxException {

    final String api = resource("/petstore.json");

    final ParserWrapper wrapper = new ParserService().getParser(ApiRef.create(api), ParserType.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.AMF));
    assertThat(wrapper.getApiVendor(), is(OAS_20));
  }

  @Test
  public void oasYaml20Wrapper() throws URISyntaxException {

    final String api = resource("/petstore.yaml");

    final ParserWrapper wrapper = new ParserService().getParser(ApiRef.create(api), ParserType.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.AMF));
    assertThat(wrapper.getApiVendor(), is(OAS_20));
  }

  @Test
  public void fallbackParser() {
    ParserService parserService = new ParserService();
    ParserWrapper wrapper = parserService.getParser(ApiRef.create(resource("/api-with-fallback-parser.raml")));

    assertNotNull(wrapper);
    assertThat(parserService.getParsingErrors().size(), is(1));
    assertThat(parserService.getParsingErrors().get(0).cause().contains("Validation failed using parser type : AMF, in file :"),
               is(true));
  }

  @Test
  public void invalidRAML() {
    expectedException.expect(ParserServiceException.class);
    ParserService parserService = new ParserService();
    try {
      parserService.getParser(ApiRef.create(resource("/with-invalid-errors.raml")));
    } finally {
      assertThat(parserService.getParsingErrors().size(), is(2));
      assertThat(parserService.getParsingErrors().get(0).cause().contains("Validation failed using parser type : AMF, in file :"),
                 is(true));
      assertThat(parserService.getParsingErrors().get(1).cause()
          .contains("Validation failed using fallback parser type : RAML, in file :"),
                 is(true));

    }
  }

  @Test
  public void getAllReferences(){
    String expected = resource("/example-with-include/schemas/team.raml");
    String api = resource("/example-with-include/example-with-include.raml");

    //AMF
    ParserWrapper wrapperAMF = new ParserService().getParser(ApiRef.create(api), ParserType.AMF);
    assertNotNull(wrapperAMF);
    assertTrue(wrapperAMF instanceof ParserWrapperAmf);
    assertEquals(1,wrapperAMF.build().getAllReferences().size());
    assertEquals(expected.replaceFirst("file:","file://"), wrapperAMF.build().getAllReferences().get(0));

    //RAMLv2
    ParserWrapper wrapperRAMLv2 = new ParserService().getParser(ApiRef.create(api), ParserType.RAML);
    assertTrue(wrapperRAMLv2 instanceof ParserWrapperV2);
    assertNotNull(wrapperRAMLv2);
    assertEquals(1,wrapperRAMLv2.build().getAllReferences().size());
    assertEquals(expected, wrapperRAMLv2.build().getAllReferences().get(0));

    //RAMLv1
    String expectedFor08 = resource("/example-with-include/schemas/atom.xsd");
    String api08 = resource("/example-with-include/api-08-with-include.raml");
    ParserWrapper wrapperRAML08 = new ParserService().getParser(ApiRef.create(api08), ParserType.RAML);
    assertTrue(wrapperRAML08 instanceof ParserWrapperV1);
    assertNotNull(wrapperRAML08);
    assertEquals(1,wrapperRAML08.build().getAllReferences().size());
    assertEquals(expectedFor08, wrapperRAML08.build().getAllReferences().get(0));

  }

  private static String resource(final String path) {
    return ResourcesUtils.resource(ParserServiceTestCase.class, path);
  }
}
