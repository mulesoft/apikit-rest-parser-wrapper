/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.StrField;
import amf.client.model.domain.AnyShape;
import amf.client.model.domain.ArrayShape;
import amf.client.model.domain.DataNode;
import amf.client.model.domain.FileShape;
import amf.client.model.domain.NilShape;
import amf.client.model.domain.NodeShape;
import amf.client.model.domain.PropertyShape;
import amf.client.model.domain.ScalarNode;
import amf.client.model.domain.ScalarShape;
import amf.client.model.domain.Shape;
import amf.client.model.domain.UnionShape;
import amf.client.validate.ValidationReport;
import amf.client.validate.ValidationResult;
import com.google.common.collect.ImmutableSet;
import org.mule.amf.impl.exceptions.UnsupportedSchemaException;
import org.mule.amf.impl.util.LazyValue;
import org.mule.apikit.model.parameter.FileProperties;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.metadata.api.model.MetadataType;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.mule.apikit.ParserUtils.getArrayAsYamlValue;

class ParameterImpl implements Parameter {

  private static final Set<String> NUMBER_DATA_TYPES = ImmutableSet.of("integer", "float", "number", "long", "double");
  public static final String BOOLEAN_DATA_TYPE = "boolean";

  private final ParameterValidationStrategy validationStrategy;
  private AnyShape schema;
  private Set<String> allowedEncoding;
  private boolean required;

  private LazyValue<Boolean> isArray = new LazyValue<>(() -> schema instanceof ArrayShape ||
      schema instanceof UnionShape && hasAnArrayVariant((UnionShape) schema));

  private LazyValue<Boolean> isNullable = new LazyValue<>(() -> schema instanceof NilShape ||
      schema instanceof UnionShape && hasNilShape((UnionShape) schema));

  ParameterImpl(amf.client.model.domain.Parameter parameter) {
    this(getSchema(parameter), parameter.required().value());
  }

  ParameterImpl(PropertyShape property) {
    this(castToAnyShape(property.range()), property.minCount().value() > 0);
  }

  ParameterImpl(PropertyShape property, Set<String> allowedEncoding) {
    this(property);
    this.allowedEncoding = allowedEncoding;
  }

  ParameterImpl(AnyShape anyShape, boolean required) {
    this.schema = anyShape;
    this.required = required;
    this.validationStrategy = ParameterValidationStrategyFactory
        .getStrategy(anyShape, needsQuotes(anyShape));
  }

  @Override
  public boolean validate(String value) {
    return validatePayload(value).conforms();
  }

  ValidationReport validatePayload(String value) {
    return validationStrategy.validatePayload(value);
  }

  private static AnyShape getSchema(amf.client.model.domain.Parameter parameter) {
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
    return getErrorMessageFromReport(validatePayload(value));
  }

  private String getErrorMessageFromReport(ValidationReport validationReport) {
    return validationReport.conforms() ? "OK"
        : validationReport.results().stream()
            .findFirst()
            .map(ValidationResult::message)
            .orElse("Error");
  }

  @Override
  public String messageFromValues(Collection<?> values) {
    String arrayAsYamlValue = getArrayAsYamlValue(this, values);
    return getErrorMessageFromReport(validatePayload(arrayAsYamlValue));
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
    return isArray.get();
  }

  @Override
  public boolean isArray() {
    return isArray.get();
  }

  @Override
  public boolean isNullable() {
    return isNullable.get();
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
    return validationStrategy.preProcessValue(value);
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

  @Override
  public boolean validateArray(Collection<?> values) {
    return validate(getArrayAsYamlValue(this, values));
  }

  private static Boolean needsQuotes(Shape anyShape) {
    ScalarShape scalarShape = null;
    if (anyShape instanceof ScalarShape) {
      scalarShape = ((ScalarShape) anyShape);
    } else if (anyShape instanceof ArrayShape) {
      Shape itemsShape = ((ArrayShape) anyShape).items();
      scalarShape = itemsShape instanceof ScalarShape ? ((ScalarShape) itemsShape) : null;
    } else if (anyShape instanceof UnionShape) {
      return ((UnionShape) anyShape).anyOf().stream().anyMatch(shape -> needsQuotes(shape));
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

  private static boolean hasAnArrayVariant(UnionShape unionShape) {
    boolean hasAnArrayVariant = false;
    for (Shape shape : unionShape.anyOf()) {
      if (shape instanceof ArrayShape) {
        hasAnArrayVariant = true;
      } else if (!(shape instanceof NilShape)) {
        return false;
      }
    }

    return hasAnArrayVariant;
  }

  private static boolean hasNilShape(UnionShape unionShape) {
    return unionShape.anyOf().stream().anyMatch(NilShape.class::isInstance);
  }

}
