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

import java.util.Collections;

public class ActionImplTest {
    private static final String RESOURCE = "/test";
    private static final String ACTION = "GET";

    private ActionImpl action;

    @Before
    public void setUp() throws Exception {
        String apiLocation = AMFImplTest.class.getResource("../amf-model-render/api-to-render.raml").toURI().toString();
        ApiReference apiRef = ApiReference.create(apiLocation);
        ResourceImpl resource = (ResourceImpl) new AMFParser(apiRef, true).parse().getResource(RESOURCE);
        action = (ActionImpl) resource.getAction(ACTION);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getSecuredBy() {
        action.getSecuredBy();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getIs() {
        action.getIs();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void cleanBaseUriParameters() {
        action.cleanBaseUriParameters();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setHeaders() {
        action.setHeaders(Collections.emptyMap());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setQueryParameters() {
        action.setQueryParameters(Collections.emptyMap());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setBody() {
        action.setBody(Collections.emptyMap());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addResponse() {
        action.addResponse(null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addSecurityReference() {
        action.addSecurityReference(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addIs() {
        action.addIs(null);
    }

}