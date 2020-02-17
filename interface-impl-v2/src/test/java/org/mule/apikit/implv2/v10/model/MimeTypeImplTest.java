/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v10.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.implv2.ParserWrapperV2;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.Response;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MimeTypeImplTest {
    private static final String APPLICATION_JSON = "application/json";
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private static final String TEXT_XML = "text/xml";
    private static final String XML_EXAMPLE = "<foo/>";
    private static final String JSON_EXAMPLE = "foo";
    private static final String ORG_JSON_EXAMPLE = "{\"name\":\"Acme\"}";
    private static final String USER_JSON_EXAMPLE = "{\"name\":\"Bob\",\"lastname\":\"Marley\"}";
    private static final String LEAGUES_RESOURCE = "/leagues";
    private static final String ORG_RESOURCE = "/organization";
    private static final String USER_RESOURCE = "/user";
    private static final String LOCATION_RESOURCE = "/location";
    private static final String ACTION_GET = "GET";
    private static final String ACTION_POST = "POST";
    private static final String ACTION_PUT = "PUT";

    private static MimeTypeImpl jsonMimeTypeGet;
    private static MimeTypeImpl xmlMimeTypeGet;
    private static MimeTypeImpl jsonMimeTypePost;
    private static MimeTypeImpl xmlMimeTypePost;
    private static MimeTypeImpl formMimeType;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("/apis/10-leagues/api.raml").toURI().toString();
        RamlImpl10V2 parser = (RamlImpl10V2) new ParserWrapperV2(apiLocation, Collections.emptyList()).parse();
        Resource leaguesResource = parser.getResources().get(LEAGUES_RESOURCE);
        ActionImpl action = (ActionImpl) leaguesResource.getAction(ACTION_GET);
        Map<String, MimeType> mimeTypes = action.getResponses().get("200").getBody();
        jsonMimeTypeGet = (MimeTypeImpl) mimeTypes.get(APPLICATION_JSON);
        xmlMimeTypeGet = (MimeTypeImpl) mimeTypes.get(TEXT_XML);
        formMimeType = (MimeTypeImpl) parser.getResources().get("/leagues").getResources().get("/{leagueId}").getResources().get("/badge")
                .getAction("PUT").getBody().get(MULTIPART_FORM_DATA);
        action = (ActionImpl) leaguesResource.getAction(ACTION_POST);
        mimeTypes = action.getBody();
        jsonMimeTypePost = (MimeTypeImpl) mimeTypes.get(APPLICATION_JSON);
        xmlMimeTypePost = (MimeTypeImpl) mimeTypes.get(TEXT_XML);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getCompiledSchemaTest() {
        jsonMimeTypeGet.getCompiledSchema();
    }

    @Test
    public void getSchemaTest() {
        assertNull(jsonMimeTypeGet.getSchema());
        assertNull(xmlMimeTypeGet.getSchema());
        assertNotNull(jsonMimeTypePost.getSchema());
        assertNotNull(xmlMimeTypePost.getSchema());
    }

    @Test
    public void getFormParametersTest() {
        assertEquals(0, xmlMimeTypeGet.getFormParameters().size());
        assertEquals(0, jsonMimeTypeGet.getFormParameters().size());
        assertEquals(2, formMimeType.getFormParameters().size());
    }

    @Test
    public void getTypeTest() {
        assertEquals(TEXT_XML, xmlMimeTypeGet.getType());
        assertEquals(APPLICATION_JSON, jsonMimeTypeGet.getType());
    }

    @Test
    public void getExampleTest() throws Exception {
        assertEquals(XML_EXAMPLE, xmlMimeTypeGet.getExample());
        assertEquals(JSON_EXAMPLE, jsonMimeTypeGet.getExample());

        String apiLocation = this.getClass().getResource("/apis/10-examples/api.raml").toURI().toString();
        RamlImpl10V2 parser = (RamlImpl10V2) new ParserWrapperV2(apiLocation, Collections.emptyList()).parse();
        Resource resource = parser.getResources().get(ORG_RESOURCE);
        ActionImpl action = (ActionImpl) resource.getAction(ACTION_GET);
        Map<String, Response> responses = action.getResponses();
        Map<String, MimeType> mimeTypes = responses.get("201").getBody();
        MimeTypeImpl jsonMimeType = (MimeTypeImpl) mimeTypes.get(APPLICATION_JSON);

        assertEquals(ORG_JSON_EXAMPLE, minifyJson(jsonMimeType.getExample()));

        Resource subResource = resource.getResources().get(USER_RESOURCE);
        action = (ActionImpl) subResource.getAction(ACTION_PUT);
        mimeTypes = action.getBody();
        jsonMimeType = (MimeTypeImpl) mimeTypes.get(APPLICATION_JSON);

        assertEquals(USER_JSON_EXAMPLE, minifyJson(jsonMimeType.getExample()));

        mimeTypes = action.getResponses().get("200").getBody();
        jsonMimeType = (MimeTypeImpl) mimeTypes.get(APPLICATION_JSON);

        assertNull(jsonMimeType.getExample());

        subResource = resource.getResources().get(LOCATION_RESOURCE);
        action = (ActionImpl) subResource.getAction(ACTION_GET);
        mimeTypes = action.getBody();
        jsonMimeType = (MimeTypeImpl) mimeTypes.get(APPLICATION_JSON);

        assertNull(jsonMimeType.getExample());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getInstanceTest() {
        jsonMimeTypeGet.getInstance();
    }

    @Test
    public void validateTest() {
        assertEquals(0, xmlMimeTypeGet.validate(XML_EXAMPLE).size());
        assertEquals(0, jsonMimeTypeGet.validate(JSON_EXAMPLE).size());
    }

    private String minifyJson(String jsonValue) {
        return jsonValue.trim().replaceAll("[\\t\\n\\r\\s]+", StringUtils.EMPTY);
    }
}