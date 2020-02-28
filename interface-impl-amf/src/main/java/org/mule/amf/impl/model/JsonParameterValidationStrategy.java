/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.AnyShape;
import amf.client.validate.PayloadValidator;
import amf.client.validate.ValidationReport;
import org.mule.amf.impl.exceptions.ParserException;

import static org.mule.amf.impl.model.MediaType.APPLICATION_JSON;
import static org.mule.amf.impl.model.MediaType.APPLICATION_YAML;

abstract class JsonParameterValidationStrategy implements ParameterValidationStrategy {
  protected final PayloadValidator jsonValidator;
  final ValidationReport nullValidationReport;

  JsonParameterValidationStrategy(AnyShape anyShape){
    final PayloadValidator yamlPayloadValidator = anyShape.payloadValidator(APPLICATION_YAML)
            .orElseThrow(() -> new ParserException(APPLICATION_YAML + " validator not found for shape " + anyShape));
    this.jsonValidator = anyShape.payloadValidator(APPLICATION_JSON)
            .orElseThrow(() -> new ParserException(APPLICATION_JSON + " validator not found for shape " + anyShape));
    this.nullValidationReport = yamlPayloadValidator.syncValidate(APPLICATION_YAML, "null");
  }

  public ValidationReport validate(String payload) {
    return jsonValidator.syncValidate(APPLICATION_JSON, payload);
  }
}
