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
import org.mule.apikit.implv1.model.MimeTypeImpl;
import org.mule.apikit.implv1.model.parameter.ParameterImpl;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.model.parameter.Parameter;
import org.raml.model.parameter.Header;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ResponseImplTest {
    private static final String RESOURCE = "/leagues";
    private static final String ACTION = "GET";
    private static final String APPLICATION_XML = "application/xml";
    private static final String CONTENT_TYPE = "content-type";
    private Response response;

    @Parameterized.Parameter
    public ApiVendor apiVendor;

    @Parameterized.Parameter(1)
    public ApiSpecification apiSpecification;

    @Parameterized.Parameters(name = "{0}")
    public static Collection apiSpecifications() throws Exception {
        String apiLocation = ResponseImplTest.class.getResource("../leagues/raml10/api.raml").toURI().toString();
        ApiReference ramlApiRef = ApiReference.create(apiLocation);

        apiLocation = ResponseImplTest.class.getResource("../leagues/oas20/api.yaml").toURI().toString();
        ApiReference oas20apiRef = ApiReference.create(apiLocation);

        apiLocation = ResponseImplTest.class.getResource("../leagues/oas30/api.yaml").toURI().toString();
        ApiReference oas30apiRef = ApiReference.create(apiLocation);

        return Arrays.asList(new Object[][]{
                {ApiVendor.RAML, new AMFParser(ramlApiRef, true).parse()},
                {ApiVendor.OAS_20, new AMFParser(oas20apiRef, true).parse()},
                {ApiVendor.OAS_30, new AMFParser(oas30apiRef, true).parse()}
        });
    }

    @Before
    public void setUp() {
        ResourceImpl resource = (ResourceImpl) apiSpecification.getResource(RESOURCE);
        response = resource.getAction(ACTION).getResponses().get("200");
    }

    @Test
    public void getBodyTest() {
        assertEquals(2, response.getBody().size());
    }

    @Test
    public void setBodyTest() {
        Map<String, MimeType> body = new HashMap<>();
        body.put(APPLICATION_XML, new MimeTypeImpl(new org.raml.model.MimeType()));
        response.setBody(body);
        // Assert that it does nothing
        assertEquals(2, response.getBody().size());
    }

    @Test
    public void hasBodyTest() {
        assertTrue(response.hasBody());
    }

    @Test
    public void getHeadersTest() {
        assertNull(response.getHeaders());
    }

    @Test
    public void setHeadersTest() {
        Map<String, Parameter> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, new ParameterImpl(new Header()));
        response.setHeaders(headers);
        // Assert that it does nothing
        assertNull(response.getHeaders());
    }

    @Test
    public void getInstanceTest() {
        assertNull(response.getInstance());
    }

    @Test
    public void getExamplesTest() {
        assertEquals(2, response.getExamples().size());
    }
}