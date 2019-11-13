/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import org.junit.Assert;
import org.junit.Test;
import org.mule.amf.impl.AMFParser;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.api.ApiReference;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

public class AMFImplTest {

    public static final String BASE_URI = "some.uri.com";
    public static final String RESOURCE = "/test";
    public static final String ACTION = "GET";

    private static AMFImpl getAmfParser() throws URISyntaxException, ExecutionException, InterruptedException {
        String apiLocation = AMFImplTest.class.getResource("../amf-model-render/api-to-render.raml").toURI().toString();
        ApiReference apiRef = ApiReference.create(apiLocation);
        return (AMFImpl) new AMFParser(apiRef, true).parse();
    }

    @Test
    public void amfImplTest() throws Exception {
        final AMFImpl amfParser = getAmfParser();

        Assert.assertNull(amfParser.getSecuritySchemes());
        Assert.assertNull(amfParser.getTraits());
        amfParser.updateBaseUri(BASE_URI);
        Assert.assertEquals(amfParser.getBaseUri(), BASE_URI);

        ResourceImpl resource = (ResourceImpl) amfParser.getResource(RESOURCE);
        assertResource(resource);
    }

    private void assertResource(ResourceImpl resource) {
        Assert.assertEquals(RESOURCE, resource.toString());
        Assert.assertEquals(1, resource.getActions().size());
        assertAction(resource.getAction(ACTION));

    }

    private void assertAction(Action action) {
        Assert.assertEquals(0, action.getBody().size());
        Assert.assertFalse(action.hasBody());
        Assert.assertEquals(1, action.getResponses().size());
        Assert.assertEquals(RESOURCE, action.getResource().toString());
        Assert.assertEquals(0, action.getQueryParameters().size());
        Assert.assertEquals(0, action.getHeaders().size());
        Assert.assertNull(action.queryString());
    }


    /* Test unsupported operation exception */

    @Test(expected = UnsupportedOperationException.class)
    public void getSecuredByTest() throws InterruptedException, ExecutionException, URISyntaxException {
        AMFImpl amfParser = getAmfParser();
        ResourceImpl resource = (ResourceImpl) amfParser.getResource(RESOURCE);
        resource.setParentUri("http://test");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getBaseUriParametersTest() throws InterruptedException, ExecutionException, URISyntaxException {
        AMFImpl amfParser = getAmfParser();
        ResourceImpl resource = (ResourceImpl) amfParser.getResource(RESOURCE);
        resource.getBaseUriParameters();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void cleanBaseUriParametersTest() throws InterruptedException, ExecutionException, URISyntaxException {
        AMFImpl amfParser = getAmfParser();
        ResourceImpl resource = (ResourceImpl) amfParser.getResource(RESOURCE);
        resource.cleanBaseUriParameters();
    }

}
