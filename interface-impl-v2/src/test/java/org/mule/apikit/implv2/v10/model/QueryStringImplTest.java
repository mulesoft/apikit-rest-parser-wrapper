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
import org.mule.apikit.model.QueryString;
import org.mule.apikit.model.Resource;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class QueryStringImplTest {
    private static final String GET_ACTION = "GET";
    private QueryString locationsQueryString;
    private QueryString emailsQueryString;
    private QueryString emailQueryString;
    private QueryString historySinceQueryString;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("/apis/10-query-string/api.raml").toURI().toString();
        RamlImpl10V2 parser = (RamlImpl10V2) new ParserWrapperV2(apiLocation, Collections.emptyList()).parse();
        ActionImpl action = (ActionImpl) parser.getResources().get("/locations").getAction(GET_ACTION);
        locationsQueryString = action.queryString();
        Resource emails = parser.getResources().get("/emails");
        action = (ActionImpl) emails.getAction(GET_ACTION);
        emailsQueryString = action.queryString();
        action = (ActionImpl) emails.getResources().get("/details").getAction(GET_ACTION);
        emailQueryString = action.queryString();
        action = (ActionImpl) emails.getResources().get("/historySince").getAction(GET_ACTION);
        historySinceQueryString = action.queryString();
    }

    @Test
    public void getDefaultValueTest() {
        assertNull(locationsQueryString.getDefaultValue());
        assertNotNull(emailsQueryString.getDefaultValue());
    }

    @Test
    public void isArrayTest() {
        assertFalse(locationsQueryString.isArray());
        assertTrue(emailsQueryString.isArray());
    }

    @Test
    public void validateTest() {
        assertTrue(locationsQueryString.validate("{ \"start\": 2, \"lat\": 12, \"long\": 13 }"));
        assertFalse(locationsQueryString.validate("Not valid query string"));
        assertTrue(emailsQueryString.validate("[ { \"subject\": \"Email Subject\", \"to\": [ \"John\" ], \"body\": \"Email body\" } ]"));
    }

    @Test
    public void isScalarTest() {
        assertFalse(locationsQueryString.isScalar());
        assertFalse(emailsQueryString.isScalar());
        assertFalse(emailQueryString.isScalar());
        assertTrue(historySinceQueryString.isScalar());
    }

    @Test
    public void isFacetArrayTest() {
        assertFalse(locationsQueryString.isFacetArray("UnionShape"));
        assertTrue(emailQueryString.isFacetArray("to"));
    }

    @Test
    public void facetsTest() {
        assertEquals(0, locationsQueryString.facets().size());
        assertEquals(0, emailsQueryString.facets().size());
        assertEquals(3, emailQueryString.facets().size());
    }

}