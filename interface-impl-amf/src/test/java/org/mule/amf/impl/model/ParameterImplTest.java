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
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.model.parameter.Parameter;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ParameterImplTest {

    private static final String RESOURCE = "/books";
    private static final String ACTION = "GET";
<<<<<<< HEAD
    private static final String ISBN = "0321736079";
    private static final String ISBN_QUERY_PARAM = "isbn";
    private static final String TAGS_QUERY_PARAM = "tags";
    private static final String AUTHOR_QUERY_PARAM = "author";
    private static final String PUBLICATION_YEAR_QUERY_PARAM = "publicationYear";
=======
    private final String TEST_NULL_RESOURCE = "/testNull";
>>>>>>> Use strategy for parameter validator
    private Map<String, Parameter> queryParams;
    private Map<String, Parameter> testNullQueryParams;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("../10-query-parameters/api.raml").toURI().toString();
        ApiReference apiRef = ApiReference.create(apiLocation);
        ApiSpecification apiSpecification = new AMFParser(apiRef, true).parse();
        ResourceImpl resource = (ResourceImpl) apiSpecification.getResource(RESOURCE);
        queryParams = resource.getAction(ACTION).getQueryParameters();
        testNullQueryParams =  apiSpecification.getResource(TEST_NULL_RESOURCE).getAction(ACTION).getQueryParameters();
    }

    @Test
    public void nonNullableInteger(){
        final Parameter nonNullableInteger = testNullQueryParams.get("nonNullableInteger");
        assertFalse(nonNullableInteger.validate(null));
        assertEquals("expected type: Number, found: Null", nonNullableInteger.message(null));
        assertTrue(nonNullableInteger.validate("123"));
    }

    @Test
    public void nullableInteger(){
        final Parameter nullableInteger = testNullQueryParams.get("nullableInteger");
        assertTrue(nullableInteger.validate(null));
        assertTrue(nullableInteger.validate("123"));
    }

    @Test
    public void nonNullableString(){
        final Parameter nonNullableString = testNullQueryParams.get("nonNullableString");
        assertFalse(nonNullableString.validate(null));
        assertEquals("expected type: String, found: Null", nonNullableString.message(null));
        assertTrue(nonNullableString.validate("123"));
    }

    @Test
    public void nullableString(){
        final Parameter nullableString = testNullQueryParams.get("nullableString");
        assertTrue(nullableString.validate(null));
        assertTrue(nullableString.validate("123"));
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
        assertEquals("expected type: Number, found: String", queryParams.get(PUBLICATION_YEAR_QUERY_PARAM).message("Not valid value"));
        assertEquals("OK", queryParams.get(ISBN_QUERY_PARAM).message(ISBN));
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
        assertTrue(queryParams.get(TAGS_QUERY_PARAM).isRepeat());

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

    @Test(expected = UnsupportedOperationException.class)
    public void getMetadataTest() {
        queryParams.get(AUTHOR_QUERY_PARAM).getMetadata();
    }

    @Test
    public void isScalarTest() {
        assertTrue(queryParams.get(ISBN_QUERY_PARAM).isScalar());
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