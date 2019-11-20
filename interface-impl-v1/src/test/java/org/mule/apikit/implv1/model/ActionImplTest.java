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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ActionImplTest {

    private ActionImpl action;
    private ActionImpl actionWithUriParams;
    private ActionImpl actionWithQueryParams;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("/apis/08-leagues/api.raml").toURI().toString();
        RamlImplV1 parser = (RamlImplV1) new ParserWrapperV1(apiLocation, Collections.emptyList()).parse();
        action = (ActionImpl) parser.getResource("/positions").getAction("GET");
        actionWithUriParams = (ActionImpl) parser.getResource("/teams").getResources().get("/{teamId}").getAction("PUT");
        actionWithQueryParams = (ActionImpl) parser.getResource("/teams").getAction("GET");

    }

    @Test
    public void getType() {
        assertEquals("GET", action.getType().name());
    }

    @Test
    public void getResource() {
        assertEquals("Position Table", action.getResource().getDisplayName());
    }

    @Test
    public void getBody() {
        assertNull(action.getBody());
        assertEquals(1, actionWithUriParams.getBody().size());
    }

    @Test
    public void getBaseUriParameters() {
        action.cleanBaseUriParameters();
        assertEquals(0, action.getBaseUriParameters().size());
    }

    @Test
    public void getResolvedUriParameters() {
        action.cleanBaseUriParameters();
        assertEquals(0, action.getResolvedUriParameters().size());
        assertEquals(1, actionWithUriParams.getResolvedUriParameters().size());
    }

    @Test
    public void getQueryParameters() {
        assertEquals(0, action.getQueryParameters().size());
        assertEquals(0, actionWithUriParams.getQueryParameters().size());
        assertEquals(1, actionWithQueryParams.getQueryParameters().size());
    }

    @Test
    public void hasBody() {
        assertFalse(action.hasBody());
        assertTrue(actionWithUriParams.hasBody());
    }

    @Test
    public void getResponses() {
        assertEquals(1, action.getResponses().size());
        assertEquals(2, actionWithUriParams.getResponses().size());
        assertEquals(1, actionWithQueryParams.getResponses().size());
    }

    @Test
    public void queryString() {
        assertNull(actionWithQueryParams.queryString());
    }

    @Test
    public void getHeaders() {
        assertEquals(0, action.getHeaders().size());
    }

    @Test
    public void getSecuredBy() {
        assertEquals(0, action.getSecuredBy().size());

    }

    @Test
    public void getIs() {
        assertEquals(0, action.getIs().size());
    }

}