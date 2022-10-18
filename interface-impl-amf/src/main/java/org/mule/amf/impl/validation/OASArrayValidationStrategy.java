/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.validation;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.ArrayShape;
import amf.client.validate.PayloadValidator;
import amf.client.validate.ValidationReport;
import com.google.common.base.Joiner;
import org.json.simple.JSONValue;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.amf.impl.util.LazyValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.mule.amf.impl.util.AMFUtils.APPLICATION_JSON;
import static org.mule.amf.impl.util.AMFUtils.needsQuotes;
import static org.mule.apikit.ParserUtils.quoteValue;

public class OASArrayValidationStrategy implements ParameterValidationStrategy {

  AnyShape arrayShape;
  JsonParameterValidationStrategy facet;
  private final LazyValue<PayloadValidator> jsonValidator =
      new LazyValue<>(() -> arrayShape.payloadValidator(APPLICATION_JSON)
          .orElseThrow(() -> new ParserException(APPLICATION_JSON + " validator not found for shape " + arrayShape)));

  public OASArrayValidationStrategy(ArrayShape arrayShape) {
    AnyShape anyShape = (AnyShape) arrayShape.items();

    this.arrayShape = arrayShape;
    this.facet = new JsonParameterValidationStrategy(anyShape);
  }

  @Override
  public ValidationReport validatePayload(String value) {
    HashMap<String, String> map = new HashMap<>();
    String[] parts = value.split(",");

    List<String> array = Arrays.stream(parts).map(part -> facet.preProcessValue(part)).collect(Collectors.toList());

    String jsonValue = "[" + Joiner.on(",").join(array) + "]";

    return jsonValidator.get().syncValidate(APPLICATION_JSON, jsonValue);
  }

  @Override
  public String preProcessValue(String value) {
    return null;
  }

}
