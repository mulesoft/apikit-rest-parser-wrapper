/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.apikit.implv1.model;

import org.junit.Test;
import org.raml.model.Template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TemplateImplTest {
    private static final String TEMPLATE = "template";

    @Test
    public void getDisplayNameTest() {
        TemplateImpl template = new TemplateImpl(new Template());
        assertNull(template.getDisplayName());
        template.setDisplayName(TEMPLATE);
        assertEquals(TEMPLATE, template.getDisplayName());
    }
}