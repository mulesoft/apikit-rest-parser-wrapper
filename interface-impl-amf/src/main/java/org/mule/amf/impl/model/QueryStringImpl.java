/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.apicontract.client.platform.APIConfiguration;
import amf.core.client.common.validation.ValidationMode;
import amf.core.client.platform.model.domain.PropertyShape;
import amf.core.client.platform.model.domain.Shape;
import amf.core.client.platform.validation.AMFValidationReport;
import amf.core.client.platform.validation.payload.AMFShapePayloadValidator;
import amf.core.internal.remote.Spec;
import amf.shapes.client.platform.model.domain.AnyShape;
import amf.shapes.client.platform.model.domain.ArrayShape;
import amf.shapes.client.platform.model.domain.NodeShape;
import amf.shapes.client.platform.model.domain.ScalarShape;
import amf.shapes.client.platform.model.domain.UnionShape;
import org.mule.apikit.model.QueryString;
import org.mule.apikit.model.parameter.Parameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.singletonList;
import static org.mule.amf.impl.model.MediaType.APPLICATION_YAML;
import static org.mule.amf.impl.model.MediaType.getMimeTypeForValue;

public class QueryStringImpl implements QueryString {

  private final Spec spec;
  private AnyShape schema;

  private final Map<String, AMFShapePayloadValidator> payloadValidatorMap = new HashMap<>();
  private final String defaultMediaType = APPLICATION_YAML;

  public QueryStringImpl(AnyShape anyShape, Spec spec) {
    this.schema = anyShape;
    this.spec = spec;
  }

  @Override
  public String getDefaultValue() {
    return schema.defaultValueStr().option().orElse(null);
  }

  @Override
  public boolean isArray() {
    return schema instanceof ArrayShape;
  }

  @Override
  public boolean validate(final String value) {
    return validatePayload(value).conforms();
  }

  private AMFValidationReport validatePayload(String value) {
    final String mimeType = getMimeTypeForValue(value);

    AMFShapePayloadValidator payloadValidator;
    if (!payloadValidatorMap.containsKey(mimeType)) {
      payloadValidator =
          APIConfiguration.API().elementClient().payloadValidatorFor(schema, mimeType, ValidationMode.StrictValidationMode());
      if (payloadValidator == null) {
        payloadValidator = APIConfiguration.API().elementClient().payloadValidatorFor(schema, defaultMediaType,
                                                                                      ValidationMode.StrictValidationMode());
      }

      payloadValidatorMap.put(mimeType, payloadValidator);
    } else {
      payloadValidator = payloadValidatorMap.get(mimeType);
    }

    if (payloadValidator != null) {
      return payloadValidator.syncValidate(value);
    } else {
      throw new RuntimeException("Unexpected Error validating request");
    }
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
          result.put(type.name().value(), new ParameterImpl(type, this.spec));
        }
      }
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
