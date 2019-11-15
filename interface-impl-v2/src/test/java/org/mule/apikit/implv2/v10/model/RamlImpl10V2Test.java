/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v10.model;

import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.implv2.ParserWrapperV2;

import java.util.Collections;

import static org.junit.Assert.*;

public class RamlImpl10V2Test {
    private RamlImpl10V2 parser;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("/apis/10-leagues/api.raml").toURI().toString();
        parser = (RamlImpl10V2)new ParserWrapperV2(apiLocation, Collections.emptyList()).parse();
    }

    @Test
    public void getResources() {
        assertEquals(1, parser.getResources().size());
    }

    @Test
    public void getBaseUri() {
        assertEquals("http://localhost/api", parser.getBaseUri());
    }

    @Test
    public void getLocation() {
        assertTrue(parser.getLocation().endsWith("apis/10-leagues/api.raml"));
    }

    @Test
    public void getVersion() {
        assertEquals("v1", parser.getVersion());
    }

    @Test
    public void getSchemas() {
        assertEquals(1, parser.getSchemas().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getResource() {
        parser.getResource("/leagues");//Check difference with amf parser
    }

    @Test
    public void getConsolidatedSchemas() {
        assertEquals(0, parser.getConsolidatedSchemas().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getCompiledSchemas() {
        parser.getCompiledSchemas();
    }

    @Test
    public void getBaseUriParameters() {
        assertEquals(0, parser.getBaseUriParameters().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getSecuritySchemes() {
        parser.getSecuritySchemes();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getTraits() {
        parser.getTraits();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getUri() {
        parser.getUri();
    }

    @Test
    public void getAllReferences() {
        assertEquals(0, parser.getAllReferences().size());
    }

    @Test
    public void getType() {
        assertEquals("RAML", parser.getType().name());
    }

    @Test
    public void getApiVendor() {
        assertEquals("RAML_10", parser.getApiVendor().name());
    }
}