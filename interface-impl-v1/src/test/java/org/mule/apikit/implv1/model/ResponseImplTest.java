/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.apikit.implv1.model;

import org.apache.commons.collections.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.implv1.ParserWrapperV1;
import org.mule.apikit.implv1.model.parameter.ParameterImpl;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.parameter.Parameter;
import org.raml.model.parameter.Header;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ResponseImplTest {

    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_XML = "application/xml";
    private static final String CONTENT_TYPE = "Content-type";

    private ResponseImpl response;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("/apis/08-leagues/api.raml").toURI().toString();
        RamlImplV1 parser = (RamlImplV1) new ParserWrapperV1(apiLocation, Collections.emptyList()).parse();
        response = (ResponseImpl) parser.getResource("/positions").getAction("GET").getResponses().get("200");
    }

    @Test
    public void getBodyTest() {
        assertEquals(APPLICATION_JSON, response.getBody().get(APPLICATION_JSON).getType());
    }

    @Test
    public void hasBodyTest() {
        assertTrue(response.hasBody());
    }

    @Test
    public void getHeadersTest() {
        assertEquals(0, response.getHeaders().size());
    }

    @Test
    public void getInstanceTest() {
        assertNotNull(response.getInstance());
    }

    @Test
    public void setBodyTest() {
        Map<String, MimeType> body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.get(APPLICATION_JSON));
        body = new HashMap<>();
        body.put(APPLICATION_XML, new MimeTypeImpl(new org.raml.model.MimeType()));
        response.setBody(body);
        assertNotNull(response.getBody());
        assertNull(response.getBody().get(APPLICATION_JSON));
    }

    @Test
    public void setHeadersTest() {
        assertEquals(MapUtils.EMPTY_MAP, response.getHeaders());
        Map<String, Parameter> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, new ParameterImpl(new Header()));
        response.setHeaders(headers);
        assertNotNull(response.getHeaders().get(CONTENT_TYPE));
    }

}