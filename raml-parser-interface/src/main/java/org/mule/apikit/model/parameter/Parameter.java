/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.model.parameter;

import org.mule.metadata.api.model.MetadataType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Parameter {

  boolean isRequired();

  /**
   * @return default value as string if any, otherwise null
   * @deprecated Please use {@link Parameter#getDefaultValues()} instead
   */
  @Deprecated
  String getDefaultValue();

  /**
   * @return a list of default values. Empty list if no default was found.
   * @since 2.5.3
   */
  List<String> getDefaultValues();

  boolean isRepeat();

  boolean isArray();

  boolean validate(String value);

  boolean validateArray(Collection<?> values);

  String message(String value);

  String messageFromValues(Collection<?> values);

  String getDisplayName();

  String getDescription();

  String getExample();

  Map<String, String> getExamples();

  Object getInstance();

  MetadataType getMetadata();

  @Deprecated
  boolean isScalar();

  @Deprecated
  boolean isFacetArray(String facet);

  String surroundWithQuotesIfNeeded(String value);

  Optional<FileProperties> getFileProperties();

  /**
   * Determines if the parameter allows null values.
   *
   * @since 2.4.0
   */
  boolean isNullable();

  default Optional<Integer> getMaxItems() {
    return Optional.empty();
  }

  default Optional<Integer> getMinItems() {
    return Optional.empty();
  }
}
