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
import org.mule.apikit.model.parameter.Parameter;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ParameterImplTest {

    private static final String RESOURCE = "/books";
    private static final String ACTION = "GET";
    private Map<String, Parameter> queryParams;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("../10-query-parameters/api.raml").toURI().toString();
        ApiReference apiRef = ApiReference.create(apiLocation);
        ResourceImpl resource = (ResourceImpl) new AMFParser(apiRef, true).parse().getResource(RESOURCE);
        queryParams = resource.getAction(ACTION).getQueryParameters();
    }

    @Test
    public void validate() {
        assertTrue(queryParams.get("author").validate("Jose Perez"));
        assertFalse(queryParams.get("publicationYear").validate("Not valid value"));
    }

    @Test
    public void message() {
        assertEquals("expected type: Number, found: String", queryParams.get("publicationYear").message("Not valid value"));
    }

    @Test
    public void isRequired() {
        assertTrue(queryParams.get("isbn").isRequired());
        assertFalse(queryParams.get("author").isRequired());
    }

    @Test
    public void getDefaultValue() {
        assertNull(queryParams.get("isbn").getDefaultValue());
    }

    @Test
    public void isRepeat() {
        assertFalse(queryParams.get("author").isRepeat());
    }

    @Test
    public void isArray() {
        assertFalse(queryParams.get("author").isRepeat());
    }

    @Test
    public void getDisplayName() {
        assertEquals("Author", queryParams.get("author").getDisplayName());
    }

    @Test
    public void getDescription() {
        assertEquals("An author's full name", queryParams.get("author").getDescription());
    }

    @Test
    public void getExample() {
        assertEquals("Mary Roach", queryParams.get("author").getExample());
    }

    @Test
    public void getExamples() {
        assertEquals(0, queryParams.get("author").getExamples().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getInstance() {
        queryParams.get("author").getInstance();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getMetadata() {
        queryParams.get("author").getMetadata();
    }

    @Test
    public void isScalar() {
        assertFalse(queryParams.get("author").isScalar());
    }

    @Test
    public void isFacetArray() {
        assertFalse(queryParams.get("author").isFacetArray("String"));
    }

    @Test
    public void surroundWithQuotesIfNeeded() {
        final String value = "*321736079";
        assertEquals("\"" + value + "\"", queryParams.get("isbn").surroundWithQuotesIfNeeded(value));
    }
}