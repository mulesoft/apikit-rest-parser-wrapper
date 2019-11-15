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

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class QueryStringImplTest {
    private QueryString queryString;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("/apis/10-query-string/api.raml").toURI().toString();
        RamlImpl10V2 parser = (RamlImpl10V2)new ParserWrapperV2(apiLocation, Collections.emptyList()).parse();
        ActionImpl action = (ActionImpl) parser.getResources().get("/locations").getAction("GET");
        queryString = action.queryString();
    }

    @Test
    public void getDefaultValue() {
        assertNull(queryString.getDefaultValue());
    }

    @Test
    public void isArray() {
        assertFalse(queryString.isArray());
    }

    @Test
    public void validate() {
        assertFalse(queryString.validate("Not valid query string"));
    }

    @Test
    public void isScalar() {
        assertFalse(queryString.isScalar());
    }

    @Test
    public void isFacetArray() {
        assertFalse(queryString.isFacetArray("UnionShape"));
    }

    @Test
    public void facets() {
        assertEquals(0, queryString.facets().size());
    }

}