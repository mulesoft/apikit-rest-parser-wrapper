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
import static org.mule.amf.impl.model.ParameterImpl.quote;

class JsonParameterValidationStrategy implements ParameterValidationStrategy {
  private final PayloadValidator jsonValidator;
  private final ValidationReport nullValidationReport;
  private final boolean needsQuotes;

  JsonParameterValidationStrategy(AnyShape anyShape, boolean needsQuotes){
    final PayloadValidator yamlPayloadValidator = anyShape.payloadValidator(APPLICATION_YAML)
            .orElseThrow(() -> new ParserException(APPLICATION_YAML + " validator not found for shape " + anyShape));
    this.jsonValidator = anyShape.payloadValidator(APPLICATION_JSON)
            .orElseThrow(() -> new ParserException(APPLICATION_JSON + " validator not found for shape " + anyShape));
    this.nullValidationReport = yamlPayloadValidator.syncValidate(APPLICATION_YAML, "null");
    this.needsQuotes = needsQuotes;
  }

  public ValidationReport validate(String value) {
    return value == null ? nullValidationReport :
            jsonValidator.syncValidate(APPLICATION_JSON, sanitize(value, needsQuotes));
  }

  private static String sanitize(String value, boolean needsQuotes) {
    return needsQuotes ? quote(value.replaceAll("\"", "\\\\\"")) : value;
  }
}
