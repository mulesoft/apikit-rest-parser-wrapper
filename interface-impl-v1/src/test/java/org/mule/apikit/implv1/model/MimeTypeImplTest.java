/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.apikit.implv1.model;

import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.implv1.ParserWrapperV1;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MimeTypeImplTest {

    public static final String EXAMPLE = "          {\n" +
            "            \"name\": \"Barcelona\",\n" +
            "            \"id\": \"BAR\",\n" +
            "            \"homeCity\": \"Barcelona\",\n" +
            "            \"stadium\": \"Camp Nou\"\n" +
            "          }";

    private MimeTypeImpl mimeType;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("/apis/08-leagues/api.raml").toURI().toString();
        RamlImplV1 parser = (RamlImplV1) new ParserWrapperV1(apiLocation, Collections.emptyList()).parse();
        mimeType = (MimeTypeImpl) parser.getResource("/teams").getAction("POST").getBody().get("application/json");
    }


    @Test
    public void getCompiledSchema() {
        assertTrue(mimeType.getCompiledSchema().toString().endsWith("apis/08-leagues/schemas/teams-schema-output.json"));
    }

    @Test
    public void getSchema() {
        assertTrue(mimeType.getSchema().contains("Name of the city to which this team belongs"));
    }

    @Test
    public void getFormParameters() {
        assertNull(mimeType.getFormParameters());
    }

    @Test
    public void getType() {
        assertEquals("application/json", mimeType.getType());

    }

    @Test
    public void getExample() {
        assertEquals(EXAMPLE, mimeType.getExample());
    }

    @Test
    public void getInstance() {
        assertNotNull(mimeType.getInstance());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validate() {
        mimeType.validate(EXAMPLE);
    }
}