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

public class ResponseImplTest {

    private ResponseImpl response;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("/apis/08-leagues/api.raml").toURI().toString();
        RamlImplV1 parser = (RamlImplV1) new ParserWrapperV1(apiLocation, Collections.emptyList()).parse();
        response = (ResponseImpl) parser.getResource("/positions").getAction("GET").getResponses().get("200");
    }

    @Test
    public void getBody() {
        assertEquals("application/json", response.getBody().get("application/json").getType());
    }

    @Test
    public void hasBody() {
        assertTrue(response.hasBody());
    }

    @Test
    public void getHeaders() {
        assertEquals(0, response.getHeaders().size());
    }

    @Test
    public void getInstance() {
        assertNotNull(response.getInstance());
    }
}