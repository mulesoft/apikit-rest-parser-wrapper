/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.validation;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.NodeShape;
import amf.client.validate.PayloadValidator;
import amf.client.validate.ValidationReport;
import com.google.common.base.Joiner;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.amf.impl.util.AMFUtils;
import org.mule.amf.impl.util.LazyValue;

import java.util.HashMap;
import java.util.Map;

import static org.mule.amf.impl.util.AMFUtils.APPLICATION_JSON;
import static org.mule.amf.impl.util.AMFUtils.castToAnyShape;

public class OASObjectValidationStrategy implements ParameterValidationStrategy {

  NodeShape nodeShape;
  Map<String, JsonParameterValidationStrategy> properties;
  private final LazyValue<PayloadValidator> jsonValidator =
      new LazyValue<>(() -> nodeShape.payloadValidator(APPLICATION_JSON)
          .orElseThrow(() -> new ParserException(APPLICATION_JSON + " validator not found for shape " + nodeShape)));

  public OASObjectValidationStrategy(NodeShape nodeShape) {
    this.nodeShape = nodeShape;
    this.properties = new HashMap<>();
    nodeShape.properties().forEach(propertyShape -> propertyShape.name().option()
        .ifPresent(name -> {
          AnyShape anyShape = castToAnyShape(propertyShape.range());
          this.properties.put(name, new JsonParameterValidationStrategy(anyShape, AMFUtils.needsQuotes(anyShape)));
        }));
  }

  @Override
  public ValidationReport validatePayload(String value) {
    HashMap<String, String> map = new HashMap<>();
    String[] parts = value.split(",");
    for (int i = 0; i < parts.length - 1; i += 2) {
      String key = parts[i];
      String val = parts[i + 1];
      JsonParameterValidationStrategy facet = properties.get(key);
      map.put("\"" + key + "\"", facet != null ? facet.preProcessValue(val) : val);
    }

    String jsonValue = "{" + Joiner.on(",").withKeyValueSeparator(":").join(map) + "}";

    return jsonValidator.get().syncValidate(APPLICATION_JSON, jsonValue);
  }

  @Override
  public String preProcessValue(String value) {
    return value;
  }
}
