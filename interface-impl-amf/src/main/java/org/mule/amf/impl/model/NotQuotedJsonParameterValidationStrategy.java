/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.AnyShape;
import amf.client.validate.ValidationReport;

class NotQuotedJsonParameterValidationStrategy extends JsonParameterValidationStrategy implements ParameterValidationStrategy {

  NotQuotedJsonParameterValidationStrategy(AnyShape anyShape){
    super(anyShape);
  }

  @Override
  public ValidationReport validate(String value) {
    return value == null ? nullValidationReport : super.validate(value);
  }
}
