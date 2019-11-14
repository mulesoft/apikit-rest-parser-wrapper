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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class ActionImplTest {
    private ActionImpl action;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("/apis/10-leagues/api.raml").toURI().getPath();
        RamlImpl10V2 parser = (RamlImpl10V2)new ParserWrapperV2(apiLocation, Collections.emptyList()).parse();
        action = (ActionImpl) parser.getResources().get("/leagues").getAction("GET");
    }
    public void getType() {
        assertEquals("GET", action.getType().name());
    }

    @Test
    public void hasBody() {
        assertFalse(action.hasBody());
    }

    @Test
    public void getResponses() {
        assertEquals(1, action.getResponses().size());
    }

    @Test
    public void getResource() {
        assertEquals("Leagues", action.getResource().getDisplayName());
    }

    @Test
    public void getBody() {
        assertEquals(0, action.getBody().size());
    }

    @Test
    public void getQueryParameters() {
        assertEquals(0, action.getQueryParameters().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getBaseUriParameters() {
        action.getBaseUriParameters();
    }

    @Test
    public void getResolvedUriParameters() {
        assertEquals(0, action.getResolvedUriParameters().size());
    }

    @Test
    public void getHeaders() {
        assertEquals(0, action.getHeaders().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getSecuredBy() {
        action.getSecuredBy();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getIs() {
        action.getIs();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void cleanBaseUriParameters() {
        action.cleanBaseUriParameters();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setHeaders() {
        action.setHeaders(Collections.emptyMap());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setQueryParameters() {
        action.setQueryParameters(Collections.emptyMap());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setBody() {
        action.setBody(Collections.emptyMap());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addResponse() {
        action.addResponse(null,null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addSecurityReference() {
        action.addSecurityReference(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addIs() {
        action.addIs(null);
    }

    @Test
    public void queryString() {
        assertNull(action.queryString());
    }
}