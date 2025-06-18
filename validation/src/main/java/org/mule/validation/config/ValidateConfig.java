/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.config;

import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserMode;
import org.mule.parser.service.ParserService;
import org.mule.runtime.core.api.el.ExpressionManager;

public class ValidateConfig implements Configuration {

  private final ApiSpecification api;
  private final boolean queryParamsStrictValidation;
  private final boolean headersStrictValidation;
  private final ExpressionManager expressionManager;

  public ValidateConfig(String apiLocation,
      boolean queryParamsStrictValidation,
      boolean headersStrictValidation,
      ParserMode mode,
      ExpressionManager expressionManager) {
    this.api = new ParserService().parse(ApiReference.create(apiLocation), mode).get();
    this.queryParamsStrictValidation = queryParamsStrictValidation;
    this.headersStrictValidation = headersStrictValidation;
    this.expressionManager = expressionManager;
  }

  @Override
  public boolean isQueryParamsStrictValidation() {
    return queryParamsStrictValidation;
  }

  @Override
  public boolean isHeadersStrictValidation() {
    return headersStrictValidation;
  }

  @Override
  public ApiSpecification getApiSpecification() {
    return api;
  }

  @Override
  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }
}
