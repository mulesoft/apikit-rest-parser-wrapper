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
import static org.junit.Assert.assertNull;

public class ResourceImplTest {
    private static final String RESOURCE_PATH = "/positions";
    private static final String parentUri = "http://localhots:8081";
    private ResourceImpl resource;
    private ResourceImpl resourceWithUriParams;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("/apis/08-leagues/api.raml").toURI().toString();
        RamlImplV1 parser = (RamlImplV1) new ParserWrapperV1(apiLocation, Collections.emptyList()).parse();
        resource = (ResourceImpl) parser.getResource(RESOURCE_PATH);
        resourceWithUriParams = (ResourceImpl) parser.getResource("/teams").getResources().get("/{teamId}");
    }

    @Test
    public void getAction() {
        assertNull(resource.getAction("DELETE"));
        assertNotNull(resource.getAction("GET"));
    }

    @Test
    public void getUri() {
        assertEquals(RESOURCE_PATH, resource.getUri());
    }

    @Test
    public void getResolvedUri() {
        assertEquals(RESOURCE_PATH, resource.getResolvedUri("v1"));
    }

    @Test
    public void getResources() {
        assertEquals(0, resource.getResources().size());
    }

    @Test
    public void getParentUri() {
        resource.setParentUri(parentUri);
        assertEquals(parentUri, resource.getParentUri());
    }

    @Test
    public void getActions() {
        assertEquals(1, resource.getActions().size());
    }

    @Test
    public void getResolvedUriParameters() {
        assertEquals(0, resource.getResolvedUriParameters().size());
        assertEquals(1, resourceWithUriParams.getResolvedUriParameters().size());
    }

    @Test
    public void getBaseUriParameters() {
        resource.cleanBaseUriParameters();
        resourceWithUriParams.cleanBaseUriParameters();
        assertEquals(0, resource.getBaseUriParameters().size());
        assertEquals(0, resourceWithUriParams.getBaseUriParameters().size());
    }

    @Test
    public void getDisplayName() {
        assertEquals("Position Table", resource.getDisplayName());
    }

    @Test
    public void getRelativeUri() {
        assertEquals(RESOURCE_PATH, resource.getRelativeUri());
    }

    @Test
    public void toStringTest() {
        assertEquals(RESOURCE_PATH, resource.toString());
    }
}