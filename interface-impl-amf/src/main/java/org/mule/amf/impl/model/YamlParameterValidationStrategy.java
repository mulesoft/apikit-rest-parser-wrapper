/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.ArrayShape;
import amf.client.validate.PayloadValidator;
import amf.client.validate.ValidationReport;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.amf.impl.util.LazyValue;

import static org.mule.amf.impl.model.MediaType.APPLICATION_YAML;
import static org.mule.apikit.ParserUtils.escapeSpecialCharsInYamlValue;

class YamlParameterValidationStrategy extends ValidationStrategy {

  private final LazyValue<PayloadValidator> parameterValidator =
      new LazyValue<>(() -> schema.parameterValidator(APPLICATION_YAML)
          .orElseThrow(() -> new ParserException(APPLICATION_YAML + " validator not found for shape " + schema)));

  public YamlParameterValidationStrategy(AnyShape anyShape, boolean schemaNeedsQuotes) {
    super(anyShape, schemaNeedsQuotes);
  }

  @Override
  public boolean valueNeedQuotes(String value) {
    return schemaNeedsQuotes || (value != null && value.startsWith("*"));
  }

  @Override
  public boolean needsPreProcess(String value) {
    String trimmedValue = value.trim();
    return schema instanceof ArrayShape && !trimmedValue.startsWith("{") && !trimmedValue.startsWith("-");
  }

  @Override
  public ValidationReport validate(String value) {
    return parameterValidator.get().syncValidate(APPLICATION_YAML, value == null ? "null" : value);
  }

  @Override
  public String escapeCharsInValue(String value) {
    return escapeSpecialCharsInYamlValue(value);
  }

  @Override
  public String removeLeadingZeros(String value) {
    return value;
  }

}
