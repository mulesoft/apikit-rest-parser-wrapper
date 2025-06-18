/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.schemas;

import com.google.common.cache.CacheLoader;
import java.util.function.Function;
import javax.xml.validation.Schema;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.MimeType;

public class XmlSchemaCacheLoader extends CacheLoader<String, Schema> {

  private final ApiSpecification api;
  private final Function<String, MimeType> keyBuilder;

  public XmlSchemaCacheLoader(ApiSpecification api,
      Function<String, MimeType> keyBuilder) {
    this.api = api;
    this.keyBuilder = keyBuilder;
  }

  @Override
  public Schema load(String schemaLocation) {
    return resolveXmlSchema(schemaLocation);
  }


  private Schema resolveXmlSchema(String schemaCacheKey) {
    MimeType mimeType = keyBuilder.apply(schemaCacheKey);

    Object compiledSchema = mimeType.getCompiledSchema();
    if (compiledSchema instanceof Schema) {
      return (Schema) compiledSchema;
    }

    String schema = mimeType.getSchema();

    //check global schemas
    if (api.getConsolidatedSchemas().containsKey(schema)) {
      compiledSchema = api.getCompiledSchemas().get(schema);
      if (compiledSchema instanceof Schema) {
        return (Schema) compiledSchema;
      }
    }
    throw new RuntimeException("XML Schema could not be resolved for key: " + schemaCacheKey);
  }
}
