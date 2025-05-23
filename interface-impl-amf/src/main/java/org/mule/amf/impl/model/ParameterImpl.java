/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.apicontract.client.platform.AMFConfiguration;
import amf.core.client.platform.model.StrField;
import amf.core.client.platform.model.domain.ArrayNode;
import amf.core.client.platform.model.domain.DataNode;
import amf.core.client.platform.model.domain.PropertyShape;
import amf.core.client.platform.model.domain.ScalarNode;
import amf.core.client.platform.model.domain.Shape;
import amf.core.client.platform.validation.AMFValidationReport;
import amf.core.client.platform.validation.AMFValidationResult;
import amf.shapes.client.platform.model.domain.AnyShape;
import amf.shapes.client.platform.model.domain.ArrayShape;
import amf.shapes.client.platform.model.domain.FileShape;
import amf.shapes.client.platform.model.domain.NilShape;
import amf.shapes.client.platform.model.domain.NodeShape;
import amf.shapes.client.platform.model.domain.ScalarShape;
import amf.shapes.client.platform.model.domain.UnionShape;
import com.google.common.collect.ImmutableSet;
import org.mule.amf.impl.exceptions.UnsupportedSchemaException;
import org.mule.amf.impl.util.LazyValue;
import org.mule.apikit.model.parameter.FileProperties;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.metadata.api.model.MetadataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.mule.apikit.ParserUtils.getArrayAsYamlValue;

class ParameterImpl implements Parameter {

  private static final Set<String> NUMBER_DATA_TYPES = ImmutableSet.of("integer", "float", "number", "long", "double");
  public static final String BOOLEAN_DATA_TYPE = "boolean";

  private final ParameterValidationStrategy validationStrategy;
  private AnyShape schema;
  private Set<String> allowedEncoding;
  private boolean required;

  private LazyValue<List<String>> defaultValues;

  private LazyValue<Boolean> isArray = new LazyValue<>(() -> schema instanceof ArrayShape ||
      schema instanceof UnionShape && hasAnArrayVariant((UnionShape) schema));

  private LazyValue<Boolean> isNullable = new LazyValue<>(() -> schema instanceof NilShape ||
      schema instanceof UnionShape && hasNilShape((UnionShape) schema));

  ParameterImpl(amf.apicontract.client.platform.model.domain.Parameter parameter, AMFConfiguration amfConfiguration) {
    this(getSchema(parameter), parameter.required().value(), amfConfiguration);
  }

  ParameterImpl(PropertyShape property, AMFConfiguration amfConfiguration) {
    this(castToAnyShape(property.range()), property.minCount().value() > 0, amfConfiguration);
  }

  ParameterImpl(PropertyShape property, Set<String> allowedEncoding, AMFConfiguration amfConfiguration) {
    this(property, amfConfiguration);
    this.allowedEncoding = allowedEncoding;
  }

  ParameterImpl(AnyShape anyShape, boolean required, AMFConfiguration amfConfiguration) {
    this.schema = anyShape;
    this.required = required;
    this.validationStrategy = ParameterValidationStrategyFactory
        .getStrategy(anyShape, needsQuotes(anyShape), amfConfiguration.elementClient());
    this.defaultValues = new LazyValue<>(() -> getDefaultValuesFromSchema(schema));
  }

  @Override
  public boolean validate(String value) {
    return validatePayload(value).conforms();
  }

  AMFValidationReport validatePayload(String value) {
    return validationStrategy.validatePayload(value);
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
    return getErrorMessageFromReport(validatePayload(value));
  }

  private String getErrorMessageFromReport(AMFValidationReport validationReport) {
    return validationReport.conforms()
        ? "OK"
        : validationReport.results()
            .stream()
            .sorted(Comparator.comparing(AMFValidationResult::message))
            .map(AMFValidationResult::message)
            .collect(joining("\n"));
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
    List<String> defaultValues = getDefaultValues();
    return !defaultValues.isEmpty() ? defaultValues.get(0) : null;
  }

  @Override
  public List<String> getDefaultValues() {
    return defaultValues.get();
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
    Shape shape = schema;
    if (shape instanceof ArrayShape) {
      shape = ((ArrayShape) shape).items();
    }
    if (shape instanceof FileShape) {
      FileShape fileShape = (FileShape) shape;
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
    return flatMapUnionShapes(unionShape).noneMatch(shape -> !(shape instanceof ArrayShape || shape instanceof NilShape));
  }

  private static boolean hasNilShape(UnionShape unionShape) {
    return flatMapUnionShapes(unionShape).anyMatch(NilShape.class::isInstance);
  }

  private static Stream<Shape> flatMapUnionShapes(UnionShape unionShape) {
    return unionShape.anyOf().stream()
        .flatMap(shape -> shape instanceof UnionShape ? flatMapUnionShapes((UnionShape) shape) : Stream.of(shape));
  }

  public static List<String> getDefaultValuesFromSchema(AnyShape schema) {
    DataNode dataNode = schema.defaultValue();
    if (dataNode == null) {
      return emptyList();
    }
    List<String> defaultValuesFromNode = getDefaultValuesFromNode(dataNode);
    if (defaultValuesFromNode.isEmpty()) {
      return getDefaultValueAsString(schema).map(v -> asList(v)).orElse(emptyList());
    }
    return defaultValuesFromNode;
  }

  private static Optional<String> getDefaultValueAsString(AnyShape schema) {
    return ofNullable(schema.defaultValueStr().option().orElse(null));
  }

  private static List<String> getDefaultValuesFromNode(DataNode dataNode) {
    if (dataNode instanceof ScalarNode) {
      return asList(((ScalarNode) dataNode).value().value());
    } else if (dataNode instanceof ArrayNode) {
      List<DataNode> members = ((ArrayNode) dataNode).members();
      List<String> values = new ArrayList<>();
      for (DataNode member : members) {
        values.addAll(getDefaultValuesFromNode(member));
      }
      return values;
    }
    return emptyList();
  }

  @Override
  public Optional<Integer> getMaxItems() {
    if (schema instanceof ArrayShape && ((ArrayShape) schema).maxItems().nonNull()) {
      return Optional.of(((ArrayShape) schema).maxItems().value());
    }
    return Optional.empty();
  }

  @Override
  public Optional<Integer> getMinItems() {
    if (schema instanceof ArrayShape && ((ArrayShape) schema).minItems().nonNull()) {
      return Optional.of(((ArrayShape) schema).minItems().value());
    }
    return Optional.empty();
  }
}
