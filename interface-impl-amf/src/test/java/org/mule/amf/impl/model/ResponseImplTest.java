/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import org.junit.Before;
import org.junit.Test;
import org.mule.amf.impl.AMFParser;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.api.ApiReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ResponseImplTest {
    private static final String RESOURCE = "/leagues";
    private static final String ACTION = "GET";
    private Response response;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("../10-leagues/api.raml").toURI().toString();
        ApiReference apiRef = ApiReference.create(apiLocation);
        ResourceImpl resource = (ResourceImpl) new AMFParser(apiRef, true).parse().getResource(RESOURCE);
        response = resource.getAction(ACTION).getResponses().get("200");
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
        assertNull(response.getHeaders());
    }

    @Test
    public void getInstance() {
        assertNull(response.getInstance());
    }

    @Test
    public void getExamples() {
        assertEquals(2, response.getExamples().size());
    }
}