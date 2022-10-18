/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.NodeShape;
import amf.client.validate.PayloadValidator;
import amf.client.validate.ValidationReport;
import com.google.common.base.Joiner;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.amf.impl.util.LazyValue;

import java.util.HashMap;
import java.util.Map;

import static org.mule.amf.impl.model.MediaType.APPLICATION_JSON;

public class OASObjectValidationStrategy implements ParameterValidationStrategy {

  NodeShape nodeShape;
  Map<String, ParameterImpl> properties;
  private final LazyValue<PayloadValidator> jsonValidator =
      new LazyValue<>(() -> nodeShape.payloadValidator(APPLICATION_JSON)
          .orElseThrow(() -> new ParserException(APPLICATION_JSON + " validator not found for shape " + nodeShape)));

  public OASObjectValidationStrategy(NodeShape nodeShape) {
    this.nodeShape = nodeShape;
    this.properties = new HashMap<>();
    nodeShape.properties().forEach(propertyShape -> propertyShape.name().option()
        .ifPresent(name -> this.properties.put(name, new ParameterImpl(propertyShape))));
  }

  @Override
  public ValidationReport validatePayload(String value) {
    HashMap<String, String> map = new HashMap<>();
    String[] parts = value.split(",");
    for (int i = 0; i < parts.length - 1; i += 2) {
      String key = parts[i];
      String val = parts[i + 1];
      ParameterImpl facet = properties.get(key);
      map.put("\"" + key + "\"", facet != null ? facet.surroundWithQuotesIfNeeded(val) : val);
    }

    String jsonValue = "{" + Joiner.on(",").withKeyValueSeparator(":").join(map) + "}";

    return jsonValidator.get().syncValidate(APPLICATION_JSON, jsonValue);
  }

  @Override
  public String preProcessValue(String value) {
    return null;
  }
}
