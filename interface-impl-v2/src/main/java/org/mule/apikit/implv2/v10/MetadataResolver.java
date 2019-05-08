/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v10;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Optional.of;

import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.json.api.JsonTypeLoader;

import java.util.Optional;

import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

public class MetadataResolver {

  private MetadataResolver() {}

  public static Optional<MetadataType> resolve(TypeDeclaration type) {
    final String schema = type.toJsonSchema();

    return isNullOrEmpty(schema) ? of(anyType()) : fromJsonSchema(schema);
  }

  private static Optional<MetadataType> fromJsonSchema(String jsonSchema) {
    final JsonTypeLoader jsonTypeLoader = new JsonTypeLoader(jsonSchema);

    return jsonTypeLoader.load(null);
  }

  public static MetadataType anyType() {
    return ANY_METADATA_TYPE;
  }

  private static final MetadataType ANY_METADATA_TYPE = BaseTypeBuilder.create(MetadataFormat.JAVA).anyType().build();

}
