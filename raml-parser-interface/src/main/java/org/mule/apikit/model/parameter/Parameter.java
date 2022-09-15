/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.model.parameter;

import org.mule.metadata.api.model.MetadataType;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface Parameter {

  boolean isRequired();

  String getDefaultValue();

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
}
