/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.amf.impl.parser.rule;

import org.junit.Before;
import org.junit.Test;
import org.mule.amf.impl.AMFParser;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.validation.ApiValidationResult;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ApiValidationResultImplTest {

    private static final String NO_VALID_RESOURCE_PATH = "not-valid-resource";
    private static final String ERROR_MESSAGE = "expected type: Integer, found: Double";
    private List<ApiValidationResult> invalidApiResults;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("./invalid.raml").toURI().toString();
        AMFParser parser = new AMFParser(ApiReference.create(apiLocation), false);
        invalidApiResults = parser.validate().getResults();
    }

    @Test(expected = RuntimeException.class)
    public void errorOnInvalidResourcePathTest() throws Exception {
        new AMFParser(ApiReference.create(NO_VALID_RESOURCE_PATH), true);
    }

    @Test
    public void getMessageTest() {
        assertTrue(invalidApiResults.get(0).getMessage().contains(ERROR_MESSAGE));
    }

    @Test
    public void getLineTest() {
        assertEquals(Optional.empty(), invalidApiResults.get(0).getLine());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getPathTest() {
        assertNull(invalidApiResults.get(0).getPath());
    }

    @Test
    public void getSeverityTest() {
        assertEquals("ERROR", invalidApiResults.get(0).getSeverity().name());
    }

    @Test
    public void toStringTest() {
        assertTrue(invalidApiResults.get(0).toString().contains(ERROR_MESSAGE));
    }
}