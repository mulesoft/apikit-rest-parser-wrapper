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
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.api.ApiReference;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MimeTypeImplTest {

    public static final String APPLICATION_JSON = "application/json";
    public static final String TEXT_XML = "text/xml";
    public static final String XML_EXAMPLE = "<foo/>";
    public static final String JSON_EXAMPLE = "foo";
    private static final String RESOURCE = "/leagues";
    private static final String ACTION = "GET";
    private static MimeTypeImpl jsonMimeType;
    private static MimeTypeImpl xmlMimeType;
    private static MimeTypeImpl formMimeType;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("../10-leagues/api.raml").toURI().toString();
        ApiReference apiRef = ApiReference.create(apiLocation);
        ResourceImpl resource = (ResourceImpl) new AMFParser(apiRef, true).parse().getResource(RESOURCE);
        Map<String, MimeType> mimeTypes = resource.getAction(ACTION).getResponses().get("200").getBody();
        jsonMimeType = (MimeTypeImpl) mimeTypes.get(APPLICATION_JSON);
        xmlMimeType = (MimeTypeImpl) mimeTypes.get(TEXT_XML);
        formMimeType = (MimeTypeImpl) resource.getResources().get("/{leagueId}").getResources().get("/badge")
                .getAction("PUT").getBody().get("multipart/form-data");
    }

    @Test
    public void getCompiledSchema() {
        assertNull(jsonMimeType.getCompiledSchema());
        assertNull(xmlMimeType.getCompiledSchema());
    }

    @Test
    public void getSchema() {
        assertNull(jsonMimeType.getSchema());
        assertNull(xmlMimeType.getSchema());
    }

    @Test
    public void getFormParameters() {
        assertEquals(0, xmlMimeType.getFormParameters().size());
        assertEquals(0, jsonMimeType.getFormParameters().size());
        assertEquals(2, formMimeType.getFormParameters().size());
    }

    @Test
    public void getType() {
        assertEquals(TEXT_XML, xmlMimeType.getType());
        assertEquals(APPLICATION_JSON, jsonMimeType.getType());
    }

    @Test
    public void getExample() {
        assertEquals(XML_EXAMPLE, xmlMimeType.getExample());
        assertEquals(JSON_EXAMPLE, jsonMimeType.getExample());
    }

    @Test
    public void getInstance() {
        assertNull(jsonMimeType.getInstance());
        assertNull(xmlMimeType.getInstance());
    }

    @Test
    public void validate() {
        assertEquals(0, xmlMimeType.validate(XML_EXAMPLE).size());
        assertEquals(0, jsonMimeType.validate(JSON_EXAMPLE).size());
    }
}