/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.core.client.platform.model.StrField;
import amf.core.client.platform.model.domain.DataNode;
import amf.core.client.platform.model.domain.PropertyShape;
import amf.core.client.platform.model.domain.ScalarNode;
import amf.core.client.platform.model.domain.Shape;
import amf.core.client.platform.validation.AMFValidationReport;
import amf.core.client.platform.validation.AMFValidationResult;
import amf.core.internal.remote.Spec;
import amf.shapes.client.platform.model.domain.AnyShape;
import amf.shapes.client.platform.model.domain.ArrayShape;
import amf.shapes.client.platform.model.domain.FileShape;
import amf.shapes.client.platform.model.domain.NodeShape;
import amf.shapes.client.platform.model.domain.ScalarShape;
import com.google.common.collect.ImmutableSet;
import org.mule.amf.impl.exceptions.UnsupportedSchemaException;
import org.mule.apikit.model.parameter.FileProperties;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.metadata.api.model.MetadataType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

class ParameterImpl implements Parameter {

  private static final Set<String> NUMBER_DATA_TYPES = ImmutableSet.of("integer", "float", "number", "long", "double");
  public static final String BOOLEAN_DATA_TYPE = "boolean";

  private final ParameterValidationStrategy validationStrategy;
  private AnyShape schema;
  private Set<String> allowedEncoding;
  private boolean required;
  private final Boolean schemaNeedsQuotes;

  ParameterImpl(amf.apicontract.client.platform.model.domain.Parameter parameter, Spec spec) {
    this(getSchema(parameter), parameter.required().value(), spec);
  }

  ParameterImpl(PropertyShape property, Spec spec) {
    this(castToAnyShape(property.range()), property.minCount().value() > 0, spec);
  }

  ParameterImpl(PropertyShape property, Set<String> allowedEncoding, Spec spec) {
    this(property, spec);
    this.allowedEncoding = allowedEncoding;
  }

  ParameterImpl(AnyShape anyShape, boolean required, Spec spec) {
    this.schema = anyShape;
    this.required = required;
    this.schemaNeedsQuotes = needsQuotes(anyShape);
    this.validationStrategy = ParameterValidationStrategyFactory
        .getStrategy(anyShape, (anyShape instanceof ScalarShape && schemaNeedsQuotes), spec);
  }

  @Override
  public boolean validate(String value) {
    return validatePayload(value).conforms();
  }

  AMFValidationReport validatePayload(String value) {
    return validationStrategy.validate(isScalar() ? surroundWithQuotesIfNeeded(value) : value);
  }

  static String quote(String payload) {
    return "\"" + payload + "\"";
  }

  private static AnyShape getSchema(amf.apicontract.client.platform.model.domain.Parameter parameter) {
    Shape shape = parameter.schema();
    return castToAnyShape(shape);
  }

  private static AnyShape castToAnyShape(Shape shape) {
    if (shape instanceof AnyShape) {
      return (AnyShape) shape;
    }
    throw new UnsupportedSchemaException();
  }

  @Override
  public String message(String value) {
    AMFValidationReport validationReport = validatePayload(value);
    if (validationReport.conforms()) {
      return "OK";
    } else {
      return validationReport.results().stream()
          .findFirst()
          .map(AMFValidationResult::message)
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
    if (defaultValue instanceof ScalarNode) {
      return ((ScalarNode) defaultValue).value().value();
    }
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
  public String surroundWithQuotesIfNeeded(String value) {
    return (value != null && (value.startsWith("*") || schemaNeedsQuotes)) ? quote(value) : value;
  }

  @Override
  public Optional<FileProperties> getFileProperties() {
    if (schema instanceof FileShape) {
      FileShape fileShape = (FileShape) schema;
      return of(new FileProperties(fileShape.minLength().value(),
                                   fileShape.maxLength().value(),
                                   fileShape.fileTypes().stream()
                                       .map(StrField::value).collect(toSet())));
    } else if (isNotEmpty(allowedEncoding)) {
      return of(new FileProperties(0, 0, allowedEncoding));
    }
    return empty();
  }

  private static Boolean needsQuotes(Shape anyShape) {
    ScalarShape scalarShape = null;
    if (anyShape instanceof ScalarShape) {
      scalarShape = ((ScalarShape) anyShape);
    } else if (anyShape instanceof ArrayShape) {
      Shape itemsShape = ((ArrayShape) anyShape).items();
      scalarShape = itemsShape instanceof ScalarShape ? ((ScalarShape) itemsShape) : null;
    }
    if (scalarShape == null) {
      return Boolean.FALSE;
    }
    String dataType = scalarShape.dataType().value();
    if (dataType == null) {
      return Boolean.FALSE;
    }
    dataType = dataType.substring(dataType.lastIndexOf('#') + 1);
    return !(NUMBER_DATA_TYPES.contains(dataType) || BOOLEAN_DATA_TYPE.equals(dataType));
  }
}
