/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.model;

import org.mule.apikit.model.SecurityScheme;

public class SecuritySchemeImpl implements SecurityScheme {

  org.raml.model.SecurityScheme securityScheme;

  public SecuritySchemeImpl(org.raml.model.SecurityScheme securityScheme) {
    this.securityScheme = securityScheme;
  }

  public Object getInstance() {
    return securityScheme;
  }
}
