/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.parser.factory;

import amf.client.execution.ExecutionEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.apikit.model.ApiFormat;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.api.ApiReference;

import java.net.URI;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AMFParserWrapperFactoryTest {

  private ApiReference apiRef;

  @Before
  public void init() {
    apiRef = mock(ApiReference.class);
  }

  @Test
  public void raml08ParserWrapperTest() {
    assertParserWrapper(ApiVendor.RAML_08, ApiFormat.RAML);
  }

  @Test
  public void raml10ParserWrapperTest() {
    assertParserWrapper(ApiVendor.RAML_10, ApiFormat.RAML);
  }

  @Test
  public void oas20ParserWrapperTest() {
    assertParserWrapper(ApiVendor.OAS_20, ApiFormat.YAML);
    assertParserWrapper(ApiVendor.OAS_20, ApiFormat.JSON);
  }

  @Test(expected = ParserException.class)
  public void oas30ParserWrapperTest() {
    assertParserWrapper(ApiVendor.OAS_30, ApiFormat.YAML);
  }

  @Test
  public void defaultParserWrapperTest() {
    assertParserWrapper(ApiVendor.RAML, ApiFormat.YAML);
  }

  @Test(expected = RuntimeException.class)
  public void nullEnvironmentTest() {
    AMFParserWrapperFactory.getParser(apiRef, null);
  }

  private void assertParserWrapper(ApiVendor vendor, ApiFormat format) {
    doReturn(vendor).when(apiRef).getVendor();
    doReturn(format.name()).when(apiRef).getFormat();
    doReturn(URI.create("./invalid.raml")).when(apiRef).getPathAsUri();
    doReturn(Optional.empty()).when(apiRef).getResourceLoader();
    AMFParserWrapper parserWrapper = AMFParserWrapperFactory.getParser(apiRef, new ExecutionEnvironment());
    assertNotNull(parserWrapper);
  }
}
