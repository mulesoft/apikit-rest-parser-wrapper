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
import org.mule.apikit.model.parameter.Parameter;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ParameterImplTest {

    private static final String RESOURCE = "/books";
    private static final String ACTION = "GET";
    private static final String ISBN = "0321736079";
    private static final String ISBN_QUERY_PARAM = "isbn";
    private static final String TAGS_QUERY_PARAM = "tags";
    private static final String AUTHOR_QUERY_PARAM = "author";
    private static final String PUBLICATION_YEAR_QUERY_PARAM = "publicationYear";
    private Map<String, Parameter> queryParams;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("/apis/10-query-parameters/api.raml").toURI().toString();
        RamlImpl10V2 parser = (RamlImpl10V2) new ParserWrapperV2(apiLocation, Collections.emptyList()).parse();
        ActionImpl action = (ActionImpl) parser.getResources().get(RESOURCE).getAction(ACTION);
        queryParams = action.getQueryParameters();
    }

    @Test
    public void validateTest() {
        assertTrue(queryParams.get(AUTHOR_QUERY_PARAM).validate("{ \"name\": \"Jose\", \"lastname\": \"Perez\" }"));
        assertFalse(queryParams.get(PUBLICATION_YEAR_QUERY_PARAM).validate("Not valid value"));
        assertTrue(queryParams.get(ISBN_QUERY_PARAM).validate(ISBN));
        assertFalse(queryParams.get(TAGS_QUERY_PARAM).validate("<tagExample>Not valid value</tagExample>"));
    }

    @Test
    public void messageTest() {
        assertEquals("Invalid type String, expected Float", queryParams.get(PUBLICATION_YEAR_QUERY_PARAM).message("Not valid value"));
    }

    @Test
    public void isRequiredTest() {
        assertTrue(queryParams.get(ISBN_QUERY_PARAM).isRequired());
        assertFalse(queryParams.get(AUTHOR_QUERY_PARAM).isRequired());
    }

    @Test
    public void getDefaultValueTest() {
        assertNull(queryParams.get(ISBN_QUERY_PARAM).getDefaultValue());
        assertNotNull(queryParams.get(TAGS_QUERY_PARAM).getDefaultValue());
    }

    @Test
    public void isRepeatTest() {
        assertFalse(queryParams.get(AUTHOR_QUERY_PARAM).isRepeat());
//        assertTrue(queryParams.get(TAGS_QUERY_PARAM).isRepeat()); Check difference with amf
    }

    @Test
    public void isArrayTest() {
        assertFalse(queryParams.get(AUTHOR_QUERY_PARAM).isArray());
        assertTrue(queryParams.get(TAGS_QUERY_PARAM).isArray());
    }

    @Test
    public void getDisplayNameTest() {
        assertEquals("Author", queryParams.get(AUTHOR_QUERY_PARAM).getDisplayName());
    }

    @Test
    public void getDescriptionTest() {
        assertEquals("An author's full name", queryParams.get(AUTHOR_QUERY_PARAM).getDescription());
    }

    @Test
    public void getExampleTest() {
        assertEquals(ISBN, queryParams.get(ISBN_QUERY_PARAM).getExample());
        assertNull(queryParams.get(PUBLICATION_YEAR_QUERY_PARAM).getExample());
    }

    @Test
    public void getExamplesTest() {
        assertEquals(0, queryParams.get(PUBLICATION_YEAR_QUERY_PARAM).getExamples().size());
        assertEquals(2, queryParams.get(AUTHOR_QUERY_PARAM).getExamples().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getInstanceTest() {
        queryParams.get(AUTHOR_QUERY_PARAM).getInstance();
    }

    @Test //TODO: APIKIT-2509 check difference with amf
    public void getMetadataTest() {
        assertTrue(queryParams.get(AUTHOR_QUERY_PARAM).getMetadata().getMetadataFormat().getValidMimeTypes().contains("application/json"));
    }

    @Test
    public void isScalarTest() {
        assertTrue(queryParams.get(ISBN_QUERY_PARAM).isScalar());//TODO: APIKIT-2509 check difference with amf
        assertFalse(queryParams.get(AUTHOR_QUERY_PARAM).isScalar());
    }

    @Test
    public void isFacetArrayTest() {
        assertFalse(queryParams.get(ISBN_QUERY_PARAM).isFacetArray(ISBN_QUERY_PARAM));
        assertFalse(queryParams.get(AUTHOR_QUERY_PARAM).isFacetArray("lastname"));
        assertTrue(queryParams.get(AUTHOR_QUERY_PARAM).isFacetArray("addresses"));
    }

    @Test
    public void surroundWithQuotesIfNeededTest() {
        String value = "*321736079";
        assertEquals("\"" + value + "\"", queryParams.get(ISBN_QUERY_PARAM).surroundWithQuotesIfNeeded(value));
        value = ISBN;
        assertEquals(value, queryParams.get(ISBN_QUERY_PARAM).surroundWithQuotesIfNeeded(value));
        value = "Comedy";
        assertEquals("\"" + value + "\"", queryParams.get(TAGS_QUERY_PARAM).surroundWithQuotesIfNeeded(value));
    }

}