/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mule.amf.impl.AMFParser;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.api.ApiReference;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MimeTypeImplTest {

    public static final String APPLICATION_JSON = "application/json";
    public static final String TEXT_XML = "text/xml";
    public static final String MULTIPART = "multipart/form-data";
    public static final String XML_EXAMPLE = "<foo/>";
    public static final String JSON_STRING_EXAMPLE = "\"foo\"";
    public static final String JSON_OBJECT_EXAMPLE = "{\"name\":\"Barcelona\",\"id\":\"BAR\",\"homeCity\":\"Barcelona\",\"stadium\":\"CampNou\",\"matches\":24}";
    public static final String JSON_MULTILINE_EXAMPLE = "{\"homeTeamScore\":3,\"awayTeamScore\":0}";
    public static final String JSON_ARRAY_EXAMPLE = "[{\"homeTeam\":\"BAR\",\"awayTeam\":\"RMA\",\"date\":\"2014-01-12T20:00:00Z\"}]";
    private static final String JSON_UNION_EXAMPLE = "{\"name\":\"localTeam\"}";
    private static final String JSON_INLINE_EXAMPLE = "{\"id\":\"12\",\"name\":\"internationalTeam\"}";
    private static final String LEAGUES_RESOURCE = "/leagues";
    private static final String LEAGUE_ID_RESOURCE = "/{leagueId}";
    private static final String TEAMS_RESOURCE = "/teams";
    private static final String TEAM_ID_RESOURCE = "/{teamId}";
    private static final String BADGE_RESOURCE = "/badge";
    private static final String FIXTURE_RESOURCE = "/fixture";
    private static final String GET_ACTION = "GET";
    private static final String PUT_ACTION = "PUT";
    private static final String RESPONSE_200_CODE = "200";
    private static final String RESPONSE_201_CODE = "201";
    private static MimeTypeImpl jsonMimeType;
    private static MimeTypeImpl xmlMimeType;
    private static MimeTypeImpl formMimeType;
    private static MimeTypeImpl objectJsonMimeType;
    private static MimeTypeImpl multilineJsonMimeType;
    private static MimeTypeImpl arrayJsonMimeType;
    private static MimeTypeImpl inlineJsonMimeType;
    private static MimeTypeImpl unionJsonMimeType;

    @Before
    public void setUp() throws Exception {
        String apiLocation = this.getClass().getResource("../10-leagues/api.raml").toURI().toString();
        ApiReference apiRef = ApiReference.create(apiLocation);
        ResourceImpl resource = (ResourceImpl) new AMFParser(apiRef, true).parse().getResource(LEAGUES_RESOURCE);
        Map<String, MimeType> mimeTypes = resource.getAction(GET_ACTION).getResponses().get(RESPONSE_200_CODE).getBody();
        jsonMimeType = (MimeTypeImpl) mimeTypes.get(APPLICATION_JSON);
        xmlMimeType = (MimeTypeImpl) mimeTypes.get(TEXT_XML);
        formMimeType = (MimeTypeImpl) resource.getResources().get(LEAGUE_ID_RESOURCE).getResources().get(BADGE_RESOURCE)
                .getAction(PUT_ACTION).getBody().get(MULTIPART);

        apiLocation = this.getClass().getResource("../08-leagues/api.raml").toURI().toString();
        apiRef = ApiReference.create(apiLocation);
        ApiSpecification api = new AMFParser(apiRef, true).parse();
        resource = (ResourceImpl) api.getResource(TEAMS_RESOURCE);
        objectJsonMimeType = (MimeTypeImpl) resource.getResources().get(TEAM_ID_RESOURCE).getAction(GET_ACTION).getResponses().get(RESPONSE_200_CODE).getBody().get(APPLICATION_JSON);

        resource = (ResourceImpl) api.getResource(FIXTURE_RESOURCE);
        multilineJsonMimeType = (MimeTypeImpl) resource.getResources().get("/{homeTeamId}/{awayTeamId}").getAction(PUT_ACTION).getBody().get(APPLICATION_JSON);
        arrayJsonMimeType = (MimeTypeImpl) resource.getAction(GET_ACTION).getResponses().get(RESPONSE_200_CODE).getBody().get(APPLICATION_JSON);

        apiLocation = this.getClass().getResource("../example-from-union/api.raml").toURI().toString();
        apiRef = ApiReference.create(apiLocation);
        api = new AMFParser(apiRef, true).parse();
        resource = (ResourceImpl) api.getResource(LEAGUES_RESOURCE);
        Map<String, Response> responses = resource.getResources().get(LEAGUE_ID_RESOURCE).getAction(GET_ACTION).getResponses();
        inlineJsonMimeType = (MimeTypeImpl) responses.get(RESPONSE_200_CODE).getBody().get(APPLICATION_JSON);
        unionJsonMimeType = (MimeTypeImpl) responses.get(RESPONSE_201_CODE).getBody().get(APPLICATION_JSON);
    }

    @Test
    public void getCompiledSchema() {
        assertNull(jsonMimeType.getCompiledSchema());
        assertNull(objectJsonMimeType.getCompiledSchema());
        assertNull(multilineJsonMimeType.getCompiledSchema());
        assertNull(arrayJsonMimeType.getCompiledSchema());
        assertNull(inlineJsonMimeType.getCompiledSchema());
        assertNull(unionJsonMimeType.getCompiledSchema());
        assertNull(xmlMimeType.getCompiledSchema());
        assertNull(formMimeType.getCompiledSchema());
    }

    @Test
    public void getSchema() {
        assertNull(jsonMimeType.getSchema());
        assertNotNull(objectJsonMimeType.getSchema());
        assertNotNull(multilineJsonMimeType.getSchema());
        assertNotNull(arrayJsonMimeType.getSchema());
        assertNotNull(inlineJsonMimeType.getSchema());
        assertNotNull(unionJsonMimeType.getSchema());
        assertNull(xmlMimeType.getSchema());
        assertNotNull(formMimeType.getSchema());
    }

    @Test
    public void getFormParameters() {
        assertEquals(0, xmlMimeType.getFormParameters().size());
        assertEquals(0, jsonMimeType.getFormParameters().size());
        assertEquals(0, objectJsonMimeType.getFormParameters().size());
        assertEquals(0, multilineJsonMimeType.getFormParameters().size());
        assertEquals(0, arrayJsonMimeType.getFormParameters().size());
        assertEquals(0, inlineJsonMimeType.getFormParameters().size());
        assertEquals(0, unionJsonMimeType.getFormParameters().size());
        assertEquals(2, formMimeType.getFormParameters().size());
    }

    @Test
    public void getType() {
        assertEquals(TEXT_XML, xmlMimeType.getType());
        assertEquals(APPLICATION_JSON, jsonMimeType.getType());
        assertEquals(APPLICATION_JSON, objectJsonMimeType.getType());
        assertEquals(APPLICATION_JSON, multilineJsonMimeType.getType());
        assertEquals(APPLICATION_JSON, arrayJsonMimeType.getType());
        assertEquals(APPLICATION_JSON, inlineJsonMimeType.getType());
        assertEquals(APPLICATION_JSON, unionJsonMimeType.getType());
        assertEquals(MULTIPART, formMimeType.getType());
    }

    @Test
    public void getExample() {
        assertEquals(XML_EXAMPLE, xmlMimeType.getExample());
        assertEquals(JSON_STRING_EXAMPLE, minifyJson(jsonMimeType.getExample()));
        assertEquals(JSON_OBJECT_EXAMPLE, minifyJson(objectJsonMimeType.getExample()));
        assertEquals(JSON_MULTILINE_EXAMPLE, minifyJson(multilineJsonMimeType.getExample()));
        assertEquals(JSON_ARRAY_EXAMPLE, minifyJson(arrayJsonMimeType.getExample()));
        assertEquals(JSON_INLINE_EXAMPLE, minifyJson(inlineJsonMimeType.getExample()));
        assertEquals(JSON_UNION_EXAMPLE, minifyJson(unionJsonMimeType.getExample()));
    }

    @Test
    public void getInstance() {
        assertNull(jsonMimeType.getInstance());
        assertNull(objectJsonMimeType.getInstance());
        assertNull(multilineJsonMimeType.getInstance());
        assertNull(arrayJsonMimeType.getInstance());
        assertNull(inlineJsonMimeType.getInstance());
        assertNull(unionJsonMimeType.getInstance());
        assertNull(xmlMimeType.getInstance());
        assertNull(formMimeType.getInstance());
    }

    @Test
    public void validate() {
        assertEquals(0, xmlMimeType.validate(XML_EXAMPLE).size());
        assertEquals(0, jsonMimeType.validate(JSON_STRING_EXAMPLE).size());
        assertEquals(0, objectJsonMimeType.validate(JSON_OBJECT_EXAMPLE).size());
        assertEquals(0, multilineJsonMimeType.validate(JSON_MULTILINE_EXAMPLE).size());
        assertEquals(0, arrayJsonMimeType.validate(JSON_ARRAY_EXAMPLE).size());
        assertEquals(0, inlineJsonMimeType.validate(JSON_INLINE_EXAMPLE).size());
        assertEquals(0, unionJsonMimeType.validate(JSON_UNION_EXAMPLE).size());

    }

    private String minifyJson(String jsonValue) {
        return jsonValue.trim().replaceAll("[\\t\\n\\r\\s]+", StringUtils.EMPTY);
    }
}