/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.amf.impl.AMFParser;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.ActionType;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.api.ApiReference;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ResourceImplTest {
    private static final String API_VERSION = "v1";
    private static final String LEAGUES_RESOURCE = "/leagues";
    private static final String LEAGUE_ID_RESOURCE = "/{leagueId}";
    private static final String LEAGUES_HISTORY_RESOURCE = "/history/{version}";
    private static final String LEAGUES_HISTORY_RESOLVED_URI = "/history/" + API_VERSION;
    private static final String GET_ACTION = "GET";
    private static final String POST_ACTION = "POST";
    private static final String PUT_ACTION = "PUT";
    private Resource leaguesResource;
    private Resource leagueIdResource;
    private Resource leaguesHistoryResource;

    @Parameterized.Parameter
    public ApiVendor apiVendor;

    @Parameterized.Parameter(1)
    public ApiSpecification apiSpecification;

    @Parameterized.Parameters(name = "{0}")
    public static Collection apiSpecifications() throws Exception {
        String apiLocation = ResourceImplTest.class.getResource("../leagues/raml10/api.raml").toURI().toString();
        ApiReference ramlApiRef = ApiReference.create(apiLocation);

        apiLocation = ResourceImplTest.class.getResource("../leagues/oas20/api.yaml").toURI().toString();
        ApiReference oas20apiRef = ApiReference.create(apiLocation);

        apiLocation = ResourceImplTest.class.getResource("../leagues/oas30/api.yaml").toURI().toString();
        ApiReference oas30apiRef = ApiReference.create(apiLocation);

        return Arrays.asList(new Object[][]{
                {ApiVendor.RAML, new AMFParser(ramlApiRef, true).parse()},
                {ApiVendor.OAS_20, new AMFParser(oas20apiRef, true).parse()},
                {ApiVendor.OAS_30, new AMFParser(oas30apiRef, true).parse()}
        });
    }

    @Before
    public void setUp() {
        Map<String, Resource> resources = apiSpecification.getResources();
        leaguesResource = resources.get(LEAGUES_RESOURCE);
        if (ApiVendor.RAML.equals(apiVendor)) {
            leagueIdResource = leaguesResource.getResources().get(LEAGUE_ID_RESOURCE);
        } else {
            leagueIdResource = resources.get(LEAGUES_RESOURCE + LEAGUE_ID_RESOURCE);
        }
        leaguesHistoryResource = resources.get(LEAGUES_HISTORY_RESOURCE);
    }

    @Test
    public void getRelativeUriTest() {
        assertEquals(LEAGUES_RESOURCE, leaguesResource.getRelativeUri());
        assertEquals(LEAGUES_HISTORY_RESOURCE, leaguesHistoryResource.getRelativeUri());
        if (ApiVendor.RAML.equals(apiVendor)) {
            assertEquals(LEAGUE_ID_RESOURCE, leagueIdResource.getRelativeUri());
        }
    }

    @Test
    public void getUriTest() {
        assertEquals(LEAGUES_RESOURCE, leaguesResource.getUri());
        assertEquals(LEAGUES_RESOURCE + LEAGUE_ID_RESOURCE, leagueIdResource.getUri());
        assertEquals(LEAGUES_HISTORY_RESOURCE, leaguesHistoryResource.getUri());
    }

    @Test
    public void getResolvedUriTest() {
        assertEquals(LEAGUES_RESOURCE, leaguesResource.getResolvedUri(API_VERSION));
        assertEquals(LEAGUES_RESOURCE + LEAGUE_ID_RESOURCE, leagueIdResource.getResolvedUri(API_VERSION));
        assertEquals(LEAGUES_HISTORY_RESOLVED_URI, leaguesHistoryResource.getResolvedUri(API_VERSION));
    }

    @Test
    public void getParentUriTest() {
        assertEquals(StringUtils.EMPTY, leaguesResource.getParentUri());
        assertEquals(StringUtils.EMPTY, leaguesHistoryResource.getParentUri());
        if (ApiVendor.RAML.equals(apiVendor)) {
            assertEquals(LEAGUES_RESOURCE, leagueIdResource.getParentUri());
        }
    }

    @Test
    public void getActionTest() {
        assertEquals(ActionType.GET, leaguesResource.getAction(GET_ACTION).getType());
        assertEquals(ActionType.POST, leaguesResource.getAction(POST_ACTION).getType());
        assertNull(leaguesResource.getAction(PUT_ACTION));
    }

    @Test
    public void getActionsTest() {
        Map<ActionType, Action> actions = leaguesResource.getActions();
        assertEquals(2, actions.size());
        assertNotNull(actions.get(ActionType.GET));
        assertNotNull(actions.get(ActionType.POST));
        assertNull(actions.get(ActionType.PUT));
        actions = leagueIdResource.getActions();
        assertEquals(3, actions.size());
        assertNotNull(actions.get(ActionType.GET));
        assertNull(actions.get(ActionType.POST));
        assertNotNull(actions.get(ActionType.PUT));
    }

    @Test
    public void getResourcesTest() {
        if (ApiVendor.RAML.equals(apiVendor)) {
            assertEquals(2, apiSpecification.getResources().size());
            Map<String, Resource> resources = leaguesResource.getResources();
            assertEquals(1, resources.size());
            resources = leagueIdResource.getResources();
            assertEquals(2, resources.size());
        } else {
            assertEquals(6, apiSpecification.getResources().size());
            Map<String, Resource> resources = leaguesResource.getResources();
            assertTrue(resources.isEmpty());
            resources = leagueIdResource.getResources();
            assertTrue(resources.isEmpty());
        }
    }

    @Test
    public void getDisplayNameTest() {
        // Not supported for OAS
        if (ApiVendor.RAML.equals(apiVendor)) {
            assertEquals("Leagues", leaguesResource.getDisplayName());
            assertEquals("Leagues History", leaguesHistoryResource.getDisplayName());
            assertEquals(LEAGUE_ID_RESOURCE, leagueIdResource.getDisplayName());
        }
    }

    @Test
    public void getResolvedUriParametersTest() {
        assertEquals(1, leagueIdResource.getResolvedUriParameters().size());
        // "version" is an special uri param so it is ignored
        assertEquals(0, leaguesHistoryResource.getResolvedUriParameters().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setParentUriTest() {
        leaguesResource.setParentUri("/api");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getBaseUriParametersTest() {
        leaguesResource.getBaseUriParameters();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void cleanBaseUriParametersTest() {
        leaguesResource.cleanBaseUriParameters();
    }

}
