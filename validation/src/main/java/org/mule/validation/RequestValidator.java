/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation;

import static org.mule.validation.helpers.AttributesHelper.getMediaType;
import static org.mule.validation.helpers.CharsetUtils.getCharset;
import static org.mule.validation.helpers.UrlUtils.getRequestPath;

import org.mule.validation.config.Configuration;
import org.mule.validation.exception.BadRequestException;
import org.mule.validation.exception.NotAcceptableException;
import org.mule.validation.exception.ResourceNotFoundException;
import org.mule.validation.exception.UnsupportedMediaTypeException;
import java.util.Map;
import javafx.util.Pair;
import org.apache.commons.collections.MapUtils;
import org.mule.apikit.model.Action;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.runtime.api.metadata.TypedValue;

public class RequestValidator {
  // TODO : cache actions validators
  private final Configuration config;
  private final ResourceFinder resourceFinder;

  public RequestValidator(Configuration config) {
    this.config = config;
    this.resourceFinder = new ResourceFinder(config.getApiSpecification());
  }

  public ValidRequest validateRequest(HttpRequestAttributes attributes, TypedValue payload)
      throws ResourceNotFoundException, BadRequestException, NotAcceptableException, UnsupportedMediaTypeException {

    String path = getRequestPath(attributes);

    Pair<Action, Map<String, String>> pair = resourceFinder.findAction(attributes.getMethod().toLowerCase(), path);

    HttpRequestAttributes attributesWithUriParams = addUriParams(attributes, pair.getValue());

    ActionValidator actionValidator = new ActionValidator(pair.getKey(), config);

    HttpRequestAttributes validatedAttributes = actionValidator.validateAttributes(attributesWithUriParams);

    if (pair.getKey().hasBody()) {
      payload = actionValidator
          .validateBody(getMediaType(attributes), getCharset(attributes.getHeaders(), payload),
              payload);
    }

    return new ValidRequest(validatedAttributes, payload);
  }


  private HttpRequestAttributes addUriParams(HttpRequestAttributes attributes, Map<String, String> uriParams) {
    if (MapUtils.isEmpty(uriParams)) {
      return attributes;
    }
    return new HttpRequestAttributesBuilder(attributes).uriParams(uriParams).build();
  }

}
