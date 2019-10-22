/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.mule.amf.impl.model.MediaType.APPLICATION_YAML;
import static org.mule.amf.impl.model.MediaType.getMimeTypeForValue;
import static org.mule.amf.impl.model.ScalarType.ScalarTypes.STRING_ID;

import java.util.Set;
import org.mule.amf.impl.exceptions.UnsupportedSchemaException;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.metadata.api.model.MetadataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.ArrayShape;
import amf.client.model.domain.DataNode;
import amf.client.model.domain.NodeShape;
import amf.client.model.domain.PropertyShape;
import amf.client.model.domain.ScalarNode;
import amf.client.model.domain.ScalarShape;
import amf.client.model.domain.Shape;
import amf.client.validate.PayloadValidator;
import amf.client.validate.ValidationReport;
import amf.client.validate.ValidationResult;

class ParameterImpl implements Parameter {

  private AnyShape schema;
  private boolean required;
  private Set<String> scalarTypes;

  private final Map<String, PayloadValidator> payloadValidatorMap = new HashMap<>();
  private final String defaultMediaType = APPLICATION_YAML;

  ParameterImpl(amf.client.model.domain.Parameter parameter) {
    this(getSchema(parameter), parameter.required().value());
  }

  ParameterImpl(PropertyShape property) {
    this(castToAnyShape(property.range()), property.minCount().value() > 0);
  }

  ParameterImpl(AnyShape anyShape, boolean required) {
    this.schema = anyShape;
    this.required = required;

    List<ScalarType> typeIds = asList(ScalarType.values());

    this.scalarTypes = typeIds.stream().map(ScalarType::getName).collect(Collectors.toSet());
  }

  @Override
  public boolean validate(String value) {
    return validatePayload(value).conforms();
  }

  private ValidationReport validatePayload(String value) {
    String mimeType = getMimeTypeForValue(value);
    PayloadValidator payloadValidator = resolvePayloadValidator(mimeType, value);
    try {
      return payloadValidator.validate(mimeType, value != null ? value : "null").get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Unexpected Error validating request", e);
    }
  }

  private PayloadValidator resolvePayloadValidator(String mimeType, String value) {
    if (value == null) {
      return schema.payloadValidator(mimeType).get();
    }
    if (payloadValidatorMap.containsKey(mimeType)) {
      return payloadValidatorMap.get(mimeType);
    }
    Optional<PayloadValidator> payloadValidator = schema.parameterValidator(mimeType);
    if (payloadValidator.isPresent()) {
      payloadValidatorMap.put(mimeType, payloadValidator.get());
      return payloadValidator.get();
    }
    payloadValidator = schema.parameterValidator(defaultMediaType);
    if (payloadValidator.isPresent()) {
      payloadValidatorMap.put(mimeType, payloadValidator.get());
      return payloadValidator.get();
    }
    throw new RuntimeException("Unexpected Error validating request");
  }

  private static AnyShape getSchema(amf.client.model.domain.Parameter parameter) {
    Shape shape = parameter.schema();
    return castToAnyShape(shape);
  }

  private static AnyShape castToAnyShape(Shape shape) {
    if (shape instanceof AnyShape)
      return (AnyShape) shape;
    throw new UnsupportedSchemaException();
  }

  @Override
  public String message(String value) {
    ValidationReport validationReport = validatePayload(value);
    if (validationReport.conforms())
      return "OK";
    else {
      return validationReport.results().stream()
          .findFirst()
          .map(ValidationResult::message)
          .orElse("Error");
    }
  }

  @Override
  public boolean isRequired() {
    return required;
  }

  @Override
  public String getDefaultValue() {
    DataNode defaultValue = schema.defaultValue();
    if (defaultValue instanceof ScalarNode)
      return ((ScalarNode) defaultValue).value().value();
    return schema.defaultValueStr().option().orElse(null);
  }

  @Override
  public boolean isRepeat() {
    return schema instanceof ArrayShape;
  }

  @Override
  public boolean isArray() {
    return schema instanceof ArrayShape;
  }

  @Override
  public String getDisplayName() {
    return schema.displayName().value();
  }

  @Override
  public String getDescription() {
    return schema.description().value();
  }

  @Override
  public String getExample() {
    return schema.examples().stream().filter(example -> example.name().value() == null)
        .map(example -> example.value().value())
        .findFirst()
        .orElse(null);
  }

  @Override
  public Map<String, String> getExamples() {
    return schema.examples().stream().filter(example -> example.name().value() != null)
        .collect(toMap(e -> e.name().value(), e -> e.value().value()));
  }

  @Override
  public Object getInstance() {
    throw new UnsupportedOperationException();
  }

  @Override
  public MetadataType getMetadata() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isScalar() {
    return scalarTypes.contains(schema.id());
  }

  @Override
  public boolean isFacetArray(String facet) {
    if (schema instanceof NodeShape) {
      for (PropertyShape type : ((NodeShape) schema).properties()) {
        if (facet.equals(type.name().value()))
          return type.range() instanceof ArrayShape;
      }
    }
    return false;
  }

  @Override
  public String surroundWithQuotesIfNeeded(String value) {
    if (value != null && (value.startsWith("*") || isStringArray())) {
      return "\"" + value + "\"";
    }
    return value;
  }

  private boolean isStringArray() {
    if (!(schema instanceof ArrayShape))
      return false;

    Shape items = ((ArrayShape) schema).items();

    if (!(items instanceof ScalarShape))
      return false;

    return ((ScalarShape) items).dataType().value().equals(STRING_ID);
  }
}
