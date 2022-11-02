/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.apicontract.client.platform.AMFConfiguration;
import amf.apicontract.client.platform.APIConfiguration;
import amf.core.client.common.validation.ValidationMode;
import amf.core.client.platform.validation.AMFValidationReport;
import amf.core.client.platform.validation.payload.AMFShapePayloadValidator;
import amf.shapes.client.platform.model.domain.AnyShape;
import amf.shapes.client.platform.model.domain.ArrayShape;
import org.mule.amf.impl.util.LazyValue;

import static org.mule.amf.impl.model.MediaType.APPLICATION_YAML;
import static org.mule.apikit.ParserUtils.escapeSpecialCharsInYamlValue;

class YamlParameterValidationStrategy extends ValidationStrategy {

  private final AMFConfiguration amfConfiguration;
  private AnyShape anyShape;

  private final LazyValue<AMFShapePayloadValidator> parameterValidator =
      new LazyValue<>(() -> getAmfConfiguration().elementClient().payloadValidatorFor(anyShape, APPLICATION_YAML,
                                                                                      ValidationMode
                                                                                          .ScalarRelaxedValidationMode()));

  public YamlParameterValidationStrategy(AnyShape anyShape, boolean schemaNeedsQuotes, AMFConfiguration amfConfiguration) {
    super(schemaNeedsQuotes);
    this.anyShape = anyShape;
    this.amfConfiguration = amfConfiguration;
  }

  @Override
  public boolean valueNeedQuotes(String value) {
    return schemaNeedsQuotes || (value != null && value.startsWith("*"));
  }

  @Override
  public boolean needsPreProcess(String value) {
    String trimmedValue = value.trim();
    return anyShape instanceof ArrayShape && !trimmedValue.startsWith("{") && !trimmedValue.startsWith("-");
  }

  @Override
  public AMFValidationReport validate(String value) {
    return parameterValidator.get().syncValidate(value == null ? "null" : value);
  }

  @Override
  public String escapeCharsInValue(String value) {
    return escapeSpecialCharsInYamlValue(value);
  }

  @Override
  public String removeLeadingZeros(String value) {
    return value;
  }

  private AMFConfiguration getAmfConfiguration() {
    return amfConfiguration;
  }
}
