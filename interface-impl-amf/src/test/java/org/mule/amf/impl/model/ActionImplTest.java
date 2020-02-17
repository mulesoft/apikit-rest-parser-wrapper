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
import org.mule.apikit.model.api.ApiReference;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ActionImplTest {
    private static final String RESOURCE = "/test";
    private static final String ACTION_GET = "GET";
    private static final String ACTION_POST = "POST";

    private ActionImpl actionGet;
    private ActionImpl actionPost;

    @Before
    public void setUp() throws Exception {
        String apiLocation = AMFImplTest.class.getResource("../amf-model-render/api-to-render.raml").toURI().toString();
        ApiReference apiRef = ApiReference.create(apiLocation);
        ResourceImpl resource = (ResourceImpl) new AMFParser(apiRef, true).parse().getResource(RESOURCE);
        actionGet = (ActionImpl) resource.getAction(ACTION_GET);
        actionPost = (ActionImpl) resource.getAction(ACTION_POST);
    }

    @Test
    public void getTypeTest() {
        assertEquals(ACTION_GET, actionGet.getType().name());
        assertEquals(ACTION_POST, actionPost.getType().name());
    }

    @Test
    public void hasBodyTest() {
        assertFalse(actionGet.hasBody());
        assertTrue(actionPost.hasBody());
    }

    @Test
    public void getResponsesTest() {
        assertEquals(1, actionGet.getResponses().size());
        assertEquals(0, actionPost.getResponses().size());
    }

    @Test
    public void getResourceTest() {
        assertEquals(RESOURCE, actionGet.getResource().getUri());
    }

    @Test
    public void getBodyTest() {
        assertEquals(0, actionGet.getBody().size());
        assertEquals(1, actionPost.getBody().size());
    }

    @Test
    public void getQueryParametersTest() {
        assertEquals(0, actionGet.getQueryParameters().size());
        assertEquals(1, actionPost.getQueryParameters().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getBaseUriParametersTest() {
        actionGet.getBaseUriParameters();
    }

    @Test
    public void getResolvedUriParametersTest() {
        assertEquals(0, actionGet.getResolvedUriParameters().size());
    }

    @Test
    public void getHeadersTest() {
        assertEquals(1, actionGet.getHeaders().size());
        assertEquals(0, actionPost.getHeaders().size());
    }

    @Test
    public void queryStringTest() {
        assertNull(actionGet.queryString());
        assertNull(actionPost.queryString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getSecuredByTest() {
        actionGet.getSecuredBy();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getIsTest() {
        actionGet.getIs();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void cleanBaseUriParametersTest() {
        actionGet.cleanBaseUriParameters();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setHeadersTest() {
        actionGet.setHeaders(Collections.emptyMap());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setQueryParametersTest() {
        actionGet.setQueryParameters(Collections.emptyMap());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setBodyTest() {
        actionGet.setBody(Collections.emptyMap());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addResponseTest() {
        actionGet.addResponse(null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addSecurityReferenceTest() {
        actionGet.addSecurityReference(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addIsTest() {
        actionGet.addIs(null);
    }

}