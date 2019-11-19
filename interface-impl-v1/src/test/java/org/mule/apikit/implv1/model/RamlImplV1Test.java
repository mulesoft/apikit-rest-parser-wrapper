/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.model;

import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.implv1.ParserWrapperV1;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RamlImplV1Test {

    public static final String TEAMS_RESOURCE = "/teams";
    private RamlImplV1 parser;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("/apis/08-leagues/api.raml").toURI().toString();
        parser = (RamlImplV1) new ParserWrapperV1(apiLocation, Collections.emptyList()).parse();
    }


    @Test
    public void getRaml() {
        assertEquals("La Liga", parser.getRaml().getTitle());
    }


    @Test
    public void getInstance() {
        assertNotNull(parser.getInstance());
    }


    @Test
    public void getResources() {
        assertEquals(3, parser.getResources().size());
    }

    @Test
    public void getBaseUri() {
        assertEquals("http://localhost:8080/api", parser.getBaseUri());
    }

    @Test
    public void getLocation() {
        assertTrue(parser.getLocation().endsWith("apis/08-leagues/api.raml"));
    }

    @Test
    public void getVersion() {
        assertEquals("1.0", parser.getVersion());
    }

    @Test
    public void getSchemas() {
        assertEquals(0, parser.getSchemas().size());
    }

    @Test
    public void getResource() {
        assertEquals(TEAMS_RESOURCE, parser.getResource(TEAMS_RESOURCE).getUri());
    }

    @Test
    public void getConsolidatedSchemas() {
        assertEquals(0, parser.getConsolidatedSchemas().size());
    }

    @Test
    public void getCompiledSchemas() {
        assertEquals(0, parser.getCompiledSchemas().size());
    }

    @Test
    public void getBaseUriParameters() {
        assertEquals(0, parser.getBaseUriParameters().size());
    }

    @Test
    public void getSecuritySchemes() {
        assertEquals(0, parser.getSecuritySchemes().size());
    }

    @Test
    public void getTraits() {
        assertEquals(0, parser.getTraits().size());

    }

    @Test
    public void getUri() {
        assertEquals("", parser.getUri());
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
        assertEquals("RAML_08", parser.getApiVendor().name());
    }

    @Test
    public void dump() {
        assertTrue(parser.dump("http://localhost:8080/api").contains("Getting Started"));
    }
}