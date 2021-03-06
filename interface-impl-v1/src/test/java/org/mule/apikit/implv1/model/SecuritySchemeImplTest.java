/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.apikit.implv1.model;

import org.junit.Test;
import org.raml.model.SecurityScheme;

import static org.junit.Assert.assertNotNull;

public class SecuritySchemeImplTest {

  @Test
  public void getInstanceTest() {
    SecuritySchemeImpl securitySchemeImpl = new SecuritySchemeImpl(new SecurityScheme());
    assertNotNull(securitySchemeImpl.getInstance());
  }
}
