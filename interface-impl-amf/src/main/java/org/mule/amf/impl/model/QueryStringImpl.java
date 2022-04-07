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
import amf.client.model.domain.PropertyShape;
import amf.client.model.domain.ScalarShape;
import amf.client.model.domain.Shape;
import amf.client.model.domain.UnionShape;
import amf.client.validate.PayloadValidator;
import org.mule.amf.impl.util.LazyValue;
import org.mule.apikit.common.ValidationUtils.QueryStringGroup;
import org.mule.apikit.common.ValidationUtils.QueryStringGroup.QueryStringGroupBuilder;
import org.mule.apikit.model.QueryString;
import org.mule.apikit.model.parameter.Parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.mule.amf.impl.model.MediaType.APPLICATION_YAML;
import static org.mule.apikit.common.ValidationUtils.validateQueryParams;

public class QueryStringImpl implements QueryString {

  private AnyShape schema;

  private LazyValue<PayloadValidator> payloadValidator = new LazyValue<>(() -> {
    Optional<PayloadValidator> payloadValidator = schema.payloadValidator(APPLICATION_YAML);
    return payloadValidator.orElseThrow(() -> new RuntimeException("YAML validator not found for query string"));
  });

  public QueryStringImpl(AnyShape anyShape) {
    this.schema = anyShape;
  }

  @Override
  public String getDefaultValue() {
    return schema.defaultValueStr().option().orElse(null);
  }

  @Override
  public boolean isArray() {
    return schema instanceof ArrayShape;
  }

  @Deprecated
  @Override
  public boolean validate(final String value) {
    return payloadValidator.get().syncValidate(APPLICATION_YAML, value).conforms();
  }

  @Override
  public boolean isScalar() {
    return schema instanceof ScalarShape;
  }

  @Override
  public boolean isFacetArray(String facet) {
    if (schema instanceof NodeShape) {
      for (PropertyShape type : ((NodeShape) schema).properties()) {
        if (facet.equals(type.name().value())) {
          return type.range() instanceof ArrayShape;
        }
      }
    }
    return false;
  }

  @Override
  public Map<String, Parameter> facets() {
    HashMap<String, Parameter> result = new HashMap<>();
    for (Shape schema : getSchemas()) {
      if (schema instanceof NodeShape) {
        for (PropertyShape type : ((NodeShape) schema).properties()) {
          result.put(type.name().value(), new ParameterImpl(type));
        }
      }
    }
    return result;
  }

  @Override
  public boolean validate(Map<String, List<String>> queryParams) {
    return validateQueryParams(queryParams, facets(), getRequiredQueryParamsByGroup());
  }

  private List<QueryStringGroup> getRequiredQueryParamsByGroup() {
    List<QueryStringGroup> result = new ArrayList<>();
    for (Shape shape : getSchemas()) {
      QueryStringGroupBuilder queryStringGroupBuilder = QueryStringGroup.builder();
      if (shape instanceof NodeShape) {
        NodeShape nodeShape = (NodeShape) shape;
        queryStringGroupBuilder.supportAdditionalProperties(!nodeShape.closed().value());
        for (PropertyShape property : nodeShape.properties()) {
          queryStringGroupBuilder.withProperty(property.name().value(), property.minCount().value() > 0);
        }
      }
      result.add(queryStringGroupBuilder.build());
    }
    return result;
  }

  private List<Shape> getSchemas() {
    if (schema instanceof UnionShape) {
      return ((UnionShape) schema).anyOf();
    } else if (schema instanceof ArrayShape) {
      return singletonList(((ArrayShape) schema).items());
    }
    return singletonList(schema);
  }
}
