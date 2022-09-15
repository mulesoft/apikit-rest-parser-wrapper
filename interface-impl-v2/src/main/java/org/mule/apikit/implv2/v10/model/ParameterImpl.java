/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v10.model;

import com.google.common.collect.ImmutableSet;
import org.mule.apikit.model.parameter.FileProperties;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.metadata.api.model.MetadataType;
import org.raml.v2.api.model.common.ValidationResult;
import org.raml.v2.api.model.v10.datamodel.ArrayTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.ExampleSpec;
import org.raml.v2.api.model.v10.datamodel.FileTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.ObjectTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.UnionTypeDeclaration;
import org.raml.v2.api.model.v10.system.types.AnnotableStringType;
import org.raml.v2.api.model.v10.system.types.MarkdownString;
import org.raml.v2.internal.impl.v10.type.TypeId;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.mule.apikit.ParserUtils.escapeSpecialCharsInYamlValue;
import static org.mule.apikit.ParserUtils.getArrayAsYamlValue;
import static org.mule.apikit.ParserUtils.quoteValue;
import static org.mule.apikit.implv2.v10.MetadataResolver.anyType;
import static org.mule.apikit.implv2.v10.MetadataResolver.resolve;
import static org.raml.v2.internal.impl.v10.type.TypeId.ARRAY;
import static org.raml.v2.internal.impl.v10.type.TypeId.BOOLEAN;
import static org.raml.v2.internal.impl.v10.type.TypeId.INTEGER;
import static org.raml.v2.internal.impl.v10.type.TypeId.NUMBER;
import static org.raml.v2.internal.impl.v10.type.TypeId.OBJECT;

public class ParameterImpl implements Parameter {

  private static final Set<String> NUMBER_DATA_TYPES = ImmutableSet.of(NUMBER.getType(), INTEGER.getType());
  private static final Set<String> BOOLEAN_DATA_TYPES = ImmutableSet.of(BOOLEAN.getType());
  private TypeDeclaration typeDeclaration;
  private Collection<String> scalarTypes;
  private Boolean required;
  private Optional<String> defaultValue;
  private final boolean typeNeedsQuotes;

  public ParameterImpl(TypeDeclaration typeDeclaration) {
    this.typeDeclaration = typeDeclaration;

    Set<TypeId> typeIds = newHashSet(TypeId.values());
    typeIds.remove(OBJECT);
    typeIds.remove(ARRAY);

    scalarTypes = transform(typeIds, TypeId::getType);
    this.typeNeedsQuotes = needsQuotes(typeDeclaration);
  }

  @Override
  public boolean validate(String value) {
    List<ValidationResult> results = typeDeclaration.validate(value);
    return results.isEmpty();
  }

  @Override
  public boolean validateArray(Collection<?> values) {
    return validate(getArrayAsYamlValue(this, values));
  }

  @Override
  public String message(String value) {
    List<ValidationResult> results = typeDeclaration.validate(value);
    return results.isEmpty() ? "OK" : results.get(0).getMessage();
  }

  @Override
  public String messageFromValues(Collection<?> values) {
    List<ValidationResult> results = typeDeclaration.validate(getArrayAsYamlValue(this, values));
    return results.isEmpty() ? "OK" : results.get(0).getMessage();
  }

  @Override
  public boolean isRequired() {
    if (required == null) {
      required = typeDeclaration.required();
    }
    return required;
  }

  @Override
  public String getDefaultValue() {
    if (defaultValue == null) {
      defaultValue = ofNullable(typeDeclaration.defaultValue());
    }

    return defaultValue.orElse(null);
  }

  @Override
  public boolean isRepeat() {
    // only available in RAML 0.8
    return false;
  }

  @Override
  public boolean isArray() {
    return typeDeclaration instanceof ArrayTypeDeclaration;
  }

  @Override
  public String getDisplayName() {
    final AnnotableStringType type = typeDeclaration.displayName();
    return type == null ? null : type.value();
  }

  @Override
  public String getDescription() {
    final MarkdownString description = typeDeclaration.description();
    return description == null ? null : description.value();
  }

  @Override
  public String getExample() {
    if (typeDeclaration.example() == null) {
      return null;
    }
    return typeDeclaration.example().value();
  }

  @Override
  public Map<String, String> getExamples() {
    Map<String, String> examples = new LinkedHashMap<>();
    for (ExampleSpec example : typeDeclaration.examples()) {
      examples.put(example.name(), example.value());
    }
    return examples;
  }

  @Override
  public Object getInstance() {
    throw new UnsupportedOperationException();
  }

  @Override
  public MetadataType getMetadata() {
    return resolve(typeDeclaration).orElse(anyType());
  }

  @Override
  public boolean isScalar() {
    return isOfType(typeDeclaration, scalarTypes);
  }

  @Override
  public boolean isFacetArray(String facet) {
    if (typeDeclaration instanceof ObjectTypeDeclaration) {
      for (TypeDeclaration type : ((ObjectTypeDeclaration) typeDeclaration).properties()) {
        if (type.name().equals(facet)) {
          return type instanceof ArrayTypeDeclaration;
        }
      }
    }
    return false;
  }

  @Override
  public String surroundWithQuotesIfNeeded(String value) {
    return value != null && typeNeedsQuotes ? quoteValue(escapeSpecialCharsInYamlValue(value)) : value;
  }

  @Override
  public Optional<FileProperties> getFileProperties() {
    TypeDeclaration type = typeDeclaration;
    if (type instanceof ArrayTypeDeclaration) {
      type = ((ArrayTypeDeclaration) type).items();
    }
    if (type instanceof FileTypeDeclaration) {
      FileTypeDeclaration fileTypeDeclaration = (FileTypeDeclaration) type;
      Integer minLength = fileTypeDeclaration.minLength() != null ? fileTypeDeclaration.minLength().intValue() : 0;
      Integer maxLength = fileTypeDeclaration.maxLength() != null ? fileTypeDeclaration.maxLength().intValue() : 0;
      List<String> fileTypes = fileTypeDeclaration.fileTypes() != null ? fileTypeDeclaration.fileTypes() : emptyList();

      return of(new FileProperties(minLength, maxLength, new HashSet<>(fileTypes)));
    }
    return empty();
  }

  /**
   * Returns whether the type or parent's types are part of the type collection.
   *
   * @param type
   * @param typesCollection
   * @return true if typeSet contains type or parent's types
   */
  private static boolean isOfType(TypeDeclaration type, Collection<String> typesCollection) {
    if (type.type() == null) {
      String[] types = type.name().split("\\|");
      return Arrays.stream(types).anyMatch(t -> typesCollection.contains(t.trim()));
    }
    return typesCollection.contains(type.type()) || (type.parentTypes() != null
        && !type.parentTypes().isEmpty()
        && type.parentTypes().stream().anyMatch(pt -> typesCollection.contains(pt.type())));
  }

  private boolean needsQuotes(TypeDeclaration typeDeclaration) {
    TypeDeclaration type = typeDeclaration;
    if (type instanceof ArrayTypeDeclaration) {
      type = ((ArrayTypeDeclaration) type).items();
      if (type instanceof UnionTypeDeclaration) {
        return ((UnionTypeDeclaration) type).of().stream().anyMatch(t -> needsQuotes(t));
      }
      if (type instanceof ObjectTypeDeclaration || type instanceof ArrayTypeDeclaration) {
        return Boolean.FALSE;
      }
    } else if (typeDeclaration instanceof UnionTypeDeclaration) {
      return ((UnionTypeDeclaration) typeDeclaration).of().stream().anyMatch(t -> needsQuotes(t));
    } else if (!isOfType(type, scalarTypes)) {
      return Boolean.FALSE;
    }
    return !(isOfType(type, NUMBER_DATA_TYPES) || isOfType(type, BOOLEAN_DATA_TYPES));
  }

}
