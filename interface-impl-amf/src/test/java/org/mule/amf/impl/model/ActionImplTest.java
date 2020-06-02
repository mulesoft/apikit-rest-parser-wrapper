/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.amf.impl.AMFParser;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.api.ApiReference;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ActionImplTest {
    private static final String TEST_RESOURCE = "/test";
    private static final String TEST_ID_RESOURCE = "/{testId}/{version}";
    private static final String TEST_ID_COMPLETE_RESOURCE = TEST_RESOURCE + TEST_ID_RESOURCE;
    private static final String ACTION_GET = "GET";
    private static final String ACTION_POST = "POST";

    private ActionImpl actionGet;
    private ActionImpl actionPost;

    @Parameterized.Parameter
    public ApiVendor apiVendor;

    @Parameterized.Parameter(1)
    public ApiSpecification apiSpecification;

    @Parameterized.Parameters(name = "{0}")
    public static Collection apiSpecifications() throws Exception {
        String apiLocation = ActionImplTest.class.getResource("../amf-model-render/raml/api-to-render.raml").toURI().toString();
        ApiReference ramlApiRef = ApiReference.create(apiLocation);

        apiLocation = ActionImplTest.class.getResource("../amf-model-render/oas20/api-to-render.yaml").toURI().toString();
        ApiReference oas20apiRef = ApiReference.create(apiLocation);

        apiLocation = ActionImplTest.class.getResource("../amf-model-render/oas30/api-to-render.yaml").toURI().toString();
        ApiReference oas30apiRef = ApiReference.create(apiLocation);

        return Arrays.asList(new Object[][]{
                {ApiVendor.RAML, new AMFParser(ramlApiRef, true).parse()},
                {ApiVendor.OAS_20, new AMFParser(oas20apiRef, true).parse()},
                {ApiVendor.OAS_30, new AMFParser(oas30apiRef, true).parse()}
        });
    }

    @Before
    public void setUp() {
        ResourceImpl resource = (ResourceImpl) apiSpecification.getResource(TEST_RESOURCE);
        actionPost = (ActionImpl) resource.getAction(ACTION_POST);
        if (ApiVendor.RAML.equals(apiVendor)) {
            actionGet = (ActionImpl) resource.getResources().get(TEST_ID_RESOURCE).getAction(ACTION_GET);
        } else {
            resource = (ResourceImpl) apiSpecification.getResource(TEST_ID_COMPLETE_RESOURCE);
            actionGet = (ActionImpl) resource.getAction(ACTION_GET);
        }
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
        if (ApiVendor.OAS_30.equals(apiVendor) || ApiVendor.OAS_20.equals(apiVendor)) {
            assertEquals(1, actionPost.getResponses().size());
            assertFalse(actionPost.getResponses().get("default").hasBody());
        } else {
            assertEquals(0, actionPost.getResponses().size());
        }
    }

    @Test
    public void getResourceTest() {
        assertEquals(TEST_RESOURCE, actionPost.getResource().getUri());
        assertEquals(TEST_ID_COMPLETE_RESOURCE, actionGet.getResource().getUri());
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
        assertEquals(1, actionGet.getResolvedUriParameters().size());
        assertEquals(0, actionPost.getResolvedUriParameters().size());
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