/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;


import amf.core.client.platform.validation.AMFValidationReport;

import static org.mule.apikit.ParserUtils.quoteValue;

abstract class ValidationStrategy implements ParameterValidationStrategy {


  protected final boolean schemaNeedsQuotes;

  protected ValidationStrategy(boolean schemaNeedsQuotes) {
    this.schemaNeedsQuotes = schemaNeedsQuotes;
  }

  @Override
  public AMFValidationReport validatePayload(String value) {
    if (value == null) {
      return validate(null);
    } else if (needsPreProcess(value)) {
      return validate(preProcessValue(value));
    }
    return validate(value);
  }

  @Override
  public String preProcessValue(String value) {
    if (value == null) {
      return null;
    }
    if (valueNeedQuotes(value)) {
      return quoteValue(escapeCharsInValue(value));
    }
    return removeLeadingZeros(value);
  }

  abstract boolean valueNeedQuotes(String value);

  abstract boolean needsPreProcess(String value);

  abstract AMFValidationReport validate(String value);

  abstract String escapeCharsInValue(String value);

  abstract String removeLeadingZeros(String value);

}
