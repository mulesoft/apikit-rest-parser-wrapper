package org.mule.validation;/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
import org.mule.validation.attributes.AttributesValidator;
import org.mule.validation.attributes.DefaultsHandler;
import org.mule.validation.attributes.HeadersDefaultsHandler;
import org.mule.validation.attributes.HeadersValidator;
import org.mule.validation.attributes.QueryParameterValidator;
import org.mule.validation.attributes.QueryParamsDefaultsHandler;
import org.mule.validation.attributes.QueryStringValidator;
import org.mule.validation.attributes.UriParametersValidator;
import org.mule.validation.body.BodyValidator;
import org.mule.validation.body.BodyValidatorFactory;
import com.google.common.collect.ImmutableList;
import org.mule.validation.config.Configuration;
import org.mule.validation.exception.BadRequestException;
import org.mule.validation.exception.NotAcceptableException;
import org.mule.validation.exception.UnsupportedMediaTypeException;
import java.util.List;
import org.mule.apikit.model.Action;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.metadata.TypedValue;

public class ActionValidator {
  // TODO : cache org.mule.validation.body validator
  private final List<AttributesValidator> attributesValidators;
  private final List<DefaultsHandler> defaultsHandlers;
  private final BodyValidatorFactory bodyValidatorFactory;

  public ActionValidator(Action action, Configuration config) {
    this.attributesValidators = initialiseValidators(action, config);
    this.defaultsHandlers = initialiseDefaultsHandlers(action);
    this.bodyValidatorFactory = new BodyValidatorFactory(action, config.getApiSpecification(), config.getExpressionManager());
  }

  public HttpRequestAttributes validateAttributes(HttpRequestAttributes attributes)
      throws BadRequestException, NotAcceptableException {

    HttpRequestAttributes withDefaults = attributes;

    for (DefaultsHandler defaultsHandler : defaultsHandlers) {
      withDefaults = defaultsHandler.addDefaultValues(withDefaults);
    }

    for (AttributesValidator attributesValidator : attributesValidators) {
      attributesValidator.validate(withDefaults);
    }
    return withDefaults;
  }

  public TypedValue validateBody(String requestMimeType, String charset, TypedValue payload)
      throws UnsupportedMediaTypeException, BadRequestException {
    BodyValidator bodyValidator = bodyValidatorFactory.resolveValidator(requestMimeType);
    return bodyValidator.validate(charset, payload);
  }

  private List<AttributesValidator> initialiseValidators(Action action, Configuration configuration) {
    return ImmutableList.of(
        new UriParametersValidator(action),
        new HeadersValidator(action, configuration.isHeadersStrictValidation()),
        new QueryStringValidator(action),
        new QueryParameterValidator(action, configuration.isQueryParamsStrictValidation()));
  }

  private List<DefaultsHandler> initialiseDefaultsHandlers(Action action) {
    return ImmutableList.of(
        new QueryParamsDefaultsHandler(action),
        new HeadersDefaultsHandler(action));
  }
}
