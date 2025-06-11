/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mule.parser.service.ParserMode.*;

import org.mule.validation.config.Configuration;
import org.mule.validation.config.ValidateConfig;
import org.mule.validation.exception.BadRequestException;
import org.mule.validation.exception.NotAcceptableException;
import org.mule.validation.exception.ResourceNotFoundException;
import org.mule.validation.exception.UnsupportedMediaTypeException;
import org.junit.Test;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;

public class RequestValidatorTestCase {

  @Test
  public void validateRequestWithUriParams()
      throws UnsupportedMediaTypeException, ResourceNotFoundException, BadRequestException, NotAcceptableException {

    Configuration configuration = new ValidateConfig(
        "uri-params/uri-parameters.raml",
        false, false,
        AUTO, null);

    RequestValidator requestValidator = new RequestValidator(configuration);

    HttpRequestAttributes attributes = getAttributesBuilder("GET", "/api/constrains/4").build();

    ValidRequest validRequest = requestValidator.validateRequest(attributes,
        new TypedValue<>(null, DataType.builder().mediaType("*/*; charset=UTF-8").build()));

    assertThat(validRequest.getAttributes().getUriParams().get("id"), equalTo("4"));
  }

  @Test
  public void validateRequestWithBody()
      throws UnsupportedMediaTypeException, ResourceNotFoundException, BadRequestException, NotAcceptableException {

    Configuration configuration = new ValidateConfig(
        "body/raml-10-with-schema.raml",
        false, false,
        AUTO, null
    );

    MultiMap<String, String> headers = new MultiMap<>();
    headers.put("Content-Type", "application/json");

    HttpRequestAttributes attributes = getAttributesBuilder("PUT", "/api/schema10user")
        .headers(headers)
        .build();

    String requestBody = "{\"username\":\"gbs\",\"firstName\":\"george\",\"lastName\":\"bernard shaw\",\"emailAddresses\":[\"gbs@ie\"]}";

    TypedValue<String> typedValue = new TypedValue<>(requestBody, DataType.builder().mediaType("application/json; charset=UTF-8").build());

    ValidRequest request = new RequestValidator(configuration).validateRequest(attributes, typedValue);

    assertThat(request.getPayload().getValue(), equalTo(requestBody));

  }

  private HttpRequestAttributesBuilder getAttributesBuilder(String method, String path) {
    return new HttpRequestAttributesBuilder()
        .listenerPath("/api/*")
        .method(method)
        .version("1")
        .scheme("http")
        .relativePath(path)
        .requestPath(path)
        .rawRequestPath(path)
        .requestUri("/")
        .rawRequestUri("/")
        .localAddress("")
        .queryString("")
        .remoteAddress("");
  }
}
