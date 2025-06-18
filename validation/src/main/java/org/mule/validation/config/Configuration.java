/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.config;

import org.mule.apikit.model.ApiSpecification;
import org.mule.runtime.core.api.el.ExpressionManager;

public interface Configuration {

  boolean isQueryParamsStrictValidation();

  boolean isHeadersStrictValidation();

  ApiSpecification getApiSpecification();

  ExpressionManager getExpressionManager();
}
