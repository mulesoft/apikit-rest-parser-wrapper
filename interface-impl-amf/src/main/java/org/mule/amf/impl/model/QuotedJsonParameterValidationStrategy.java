/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.AnyShape;
import amf.client.validate.ValidationReport;

import static org.mule.amf.impl.model.MediaType.APPLICATION_JSON;
import static org.mule.amf.impl.model.ParameterImpl.quote;

class QuotedJsonParameterValidationStrategy extends JsonParameterValidationStrategy implements ParameterValidationStrategy {

  QuotedJsonParameterValidationStrategy(AnyShape anyShape){
    super(anyShape);
  }

  @Override
  public ValidationReport validate(String value) {
    return value == null ? nullValidationReport : super.validate(sanitize(value));
  }

  private static String sanitize(String value) {
    return quote(value.replaceAll("\"", "\\\\\""));
  }
}
