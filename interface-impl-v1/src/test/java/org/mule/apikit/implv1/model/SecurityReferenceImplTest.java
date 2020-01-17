/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.apikit.implv1.model;

import org.junit.Before;
import org.junit.Test;
import org.raml.model.SecurityReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SecurityReferenceImplTest {

    private static final String SECURITY_REFERENCE_NAME = "securityReference";
    private SecurityReferenceImpl securityReferenceImpl;

    @Before
    public void setUp() {
        securityReferenceImpl = new SecurityReferenceImpl(new SecurityReference(SECURITY_REFERENCE_NAME));
    }

    @Test
    public void getNamTest() {
        assertEquals(SECURITY_REFERENCE_NAME, securityReferenceImpl.getName());
    }

    @Test
    public void getInstanceTest() {
        assertNotNull(securityReferenceImpl.getInstance());
    }
}