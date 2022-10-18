/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.ArrayShape;
import amf.client.model.domain.NodeShape;
import amf.client.validate.PayloadValidator;
import amf.client.validate.ValidationReport;
import com.google.common.base.Joiner;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.amf.impl.util.LazyValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.mule.amf.impl.model.MediaType.APPLICATION_JSON;

public class OASArrayValidationStrategy implements ParameterValidationStrategy {

  AnyShape arrayShape;
  ParameterImpl facet;
  private final LazyValue<PayloadValidator> jsonValidator =
      new LazyValue<>(() -> arrayShape.payloadValidator(APPLICATION_JSON)
          .orElseThrow(() -> new ParserException(APPLICATION_JSON + " validator not found for shape " + arrayShape)));

  public OASArrayValidationStrategy(ArrayShape arrayShape) {
    AnyShape anyShape = (AnyShape) arrayShape.items();

    this.arrayShape = arrayShape;
    this.facet = new ParameterImpl(anyShape, true, ParameterValidationStrategyFactory.getStrategy(anyShape));
  }

  @Override
  public ValidationReport validatePayload(String value) {
    HashMap<String, String> map = new HashMap<>();
    String[] parts = value.split(",");

    List<String> array = Arrays.stream(parts).map(part -> facet.surroundWithQuotesIfNeeded(part)).collect(Collectors.toList());

    String jsonValue = "[" + Joiner.on(",").join(array) + "]";

    return jsonValidator.get().syncValidate(APPLICATION_JSON, jsonValue);
  }

  @Override
  public String preProcessValue(String value) {
    return null;
  }
}
