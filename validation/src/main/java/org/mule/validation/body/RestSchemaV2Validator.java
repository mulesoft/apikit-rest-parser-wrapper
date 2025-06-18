/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.body;

import static java.util.stream.Collectors.joining;

import org.mule.validation.exception.BadRequestException;
import java.util.List;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.validation.ApiValidationResult;
import org.mule.runtime.api.metadata.TypedValue;

public class RestSchemaV2Validator extends BodyAsStringValidator implements BodyValidator {

  private final MimeType mimeType;

  public RestSchemaV2Validator(MimeType mimeType) {
    this.mimeType = mimeType;
  }

  @Override
  public TypedValue validate(String charset, TypedValue payload) throws BadRequestException {
    validate(getPayloadAsString(payload.getValue(), charset));
    return payload;
  }

  public void validate(String payload) throws BadRequestException {
    List<ApiValidationResult> validationResults = mimeType.validate(payload);
    if (!validationResults.isEmpty()) {
      throw new BadRequestException(buildLogMessage(validationResults));
    }
  }

  private String buildLogMessage(List<ApiValidationResult> validationResults) {
    return validationResults.stream().map(result -> result.getMessage().replace("\n", "")).collect(joining("\n"));
  }

}
