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
import org.mule.apikit.model.Response;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ResponseImplTest {
    private static final String RESOURCE = "/leagues";
    private static final String ACTION = "GET";
    private Response response;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("/apis/10-leagues/api.raml").toURI().toString();
        RamlImpl10V2 parser = (RamlImpl10V2)new ParserWrapperV2(apiLocation, Collections.emptyList()).parse();
        ActionImpl action = (ActionImpl) parser.getResources().get(RESOURCE).getAction(ACTION);
        response = action.getResponses().get("200");
    }

    @Test
    public void getBody() {
        assertEquals(2, response.getBody().size());
    }

    @Test
    public void hasBody() {
        assertTrue(response.hasBody());
    }

    @Test
    public void getHeaders() {
        assertEquals(0, response.getHeaders().size());//check difference with amf
    }

    @Test(expected = UnsupportedOperationException.class)//check difference with amf
    public void getInstance() {
        response.getInstance();
    }

    @Test
    public void getExamples() {
        assertEquals(2, response.getExamples().size());
    }
}