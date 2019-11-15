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
import org.mule.apikit.model.QueryString;
import org.mule.apikit.model.api.ApiReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class QueryStringImplTest {

    private QueryString queryString;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("../10-query-string/api.raml").toURI().toString();
        ApiReference apiRef = ApiReference.create(apiLocation);
        ResourceImpl resource = (ResourceImpl) new AMFParser(apiRef, true).parse().getResource("/locations");
        queryString = resource.getAction("GET").queryString();
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
        assertTrue(queryString.validate("{'start':2,'lat':12,'long':13}"));
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