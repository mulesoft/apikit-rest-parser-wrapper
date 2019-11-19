/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.apikit.implv1.model.parameter;

import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.implv1.ParserWrapperV1;
import org.mule.apikit.implv1.model.RamlImplV1;
import org.mule.apikit.implv1.model.ResourceImpl;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ParameterImplTest {

    private static final String EXAMPLE = "BAR";
    private ParameterImpl parameter;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("/apis/08-leagues/api.raml").toURI().toString();
        RamlImplV1 parser = (RamlImplV1) new ParserWrapperV1(apiLocation, Collections.emptyList()).parse();
        ResourceImpl resource = (ResourceImpl) parser.getResource("/teams").getResources().get("/{teamId}");
        parameter = (ParameterImpl) resource.getResolvedUriParameters().get("teamId");
    }

    @Test
    public void isRequired() {
        assertFalse(parameter.isRequired());
    }

    @Test
    public void getDefaultValue() {
        assertNull(parameter.getDefaultValue());
    }

    @Test
    public void isRepeat() {
        assertFalse(parameter.isRepeat());
    }

    @Test
    public void isArray() {
        assertFalse(parameter.isArray());
    }

    @Test
    public void validate() {
        assertFalse(parameter.validate("CARP"));
        assertTrue(parameter.validate("RIV"));
    }

    @Test
    public void message() {
        assertEquals("Value length is longer than 3", parameter.message("CARP"));
        assertEquals("OK", parameter.message("RIV"));
    }

    @Test
    public void getDisplayName() {
        assertNull(parameter.getDisplayName());
    }

    @Test
    public void getDescription() {
        assertEquals("Three letter code that identifies the team.\n", parameter.getDescription());
    }

    @Test
    public void getExample() {
        assertEquals(EXAMPLE, parameter.getExample());
    }

    @Test
    public void getExamples() {
        assertEquals(0, parameter.getExamples().size());
    }

    @Test
    public void getInstance() {
        assertNotNull(parameter.getInstance());
    }

    @Test
    public void getMetadata() {
        assertEquals("java", parameter.getMetadata().getMetadataFormat().getId());
    }

    @Test
    public void isScalar() {
        assertTrue(parameter.isScalar());
    }

    @Test
    public void isFacetArray() {
        assertFalse(parameter.isFacetArray("String"));
    }

    @Test
    public void surroundWithQuotesIfNeeded() {
        assertEquals(EXAMPLE, parameter.surroundWithQuotesIfNeeded(EXAMPLE));
    }
}