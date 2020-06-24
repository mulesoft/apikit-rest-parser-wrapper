/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.parser.factory;

import amf.ProfileName;
import amf.client.environment.Environment;
import amf.client.parse.Oas20Parser;
import amf.client.parse.Oas20YamlParser;
import amf.client.parse.Oas30YamlParser;
import amf.client.parse.Parser;
import amf.client.parse.Raml08Parser;
import amf.client.parse.Raml10Parser;
import amf.client.parse.RamlParser;
import amf.client.resolve.Oas20Resolver;
import amf.client.resolve.Raml08Resolver;
import amf.client.resolve.Raml10Resolver;
import amf.client.resolve.Resolver;
import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.model.ApiFormat;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.api.ApiReference;

import static amf.ProfileNames.AMF;
import static amf.ProfileNames.OAS20;
import static amf.ProfileNames.RAML;
import static amf.ProfileNames.RAML10;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
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
        assertRamlParserWrapper(ApiVendor.RAML_08, Raml08Parser.class, Raml10Resolver.class, RAML());
    }

    @Test
    public void raml10ParserWrapperTest() {
        assertRamlParserWrapper(ApiVendor.RAML_10, Raml10Parser.class, Raml10Resolver.class, RAML10());
    }

    @Test
    public void oas20ParserWrapperTest() {
        assertOasParserWrapper(ApiVendor.OAS_20, Oas20Parser.class, Oas20Resolver.class, OAS20());
    }

    @Test
    public void defaultParserWrapperTest() {
        assertRamlParserWrapper(ApiVendor.RAML, RamlParser.class, Raml08Resolver.class, AMF());
    }

    @Test(expected = RuntimeException.class)
    public void nullEnvironmentTest() {
        AMFParserWrapperFactory.getParser(apiRef, null);
    }

    private void assertRamlParserWrapper(ApiVendor vendor, Class parserClass, Class resolverClass, ProfileName profileName) {
        assertParserWrapper(vendor, ApiFormat.RAML, parserClass, resolverClass, profileName);
    }

    private void assertOasParserWrapper(ApiVendor vendor, Class parserClass, Class resolverClass, ProfileName profileName) {
        assertParserWrapper(vendor, ApiFormat.JSON, parserClass, resolverClass, profileName);
        Class clazz = ApiVendor.OAS_20.equals(vendor) ? Oas20YamlParser.class : Oas30YamlParser.class;
        assertParserWrapper(vendor, ApiFormat.YAML, clazz, resolverClass, profileName);
    }

    private void assertParserWrapper(ApiVendor vendor, ApiFormat format, Class<Parser> parserClass, Class<Resolver> resolverClass, ProfileName profileName) {
        doReturn(vendor).when(apiRef).getVendor();
        doReturn(format.name()).when(apiRef).getFormat();
        AMFParserWrapper parserWrapper = AMFParserWrapperFactory.getParser(apiRef, new Environment());
        assertThat(parserWrapper.getParser(), is(instanceOf(parserClass)));
        assertThat(parserWrapper.getResolver(), is(instanceOf(resolverClass)));
        assertEquals(parserWrapper.getProfileName(), profileName);
    }
}
