/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.schemas;

import com.github.fge.jsonschema.main.JsonSchema;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import javax.xml.validation.Schema;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.MimeType;

public class SchemasHandler {
  private final LoadingCache<String, JsonSchema> jsonSchemaCache;
  private final LoadingCache<String, Schema> xmlSchemaCache;
  private static final String SEPARATOR = ",";

  public SchemasHandler(ApiSpecification api) {

    Function<String, MimeType> keyBuilder = schemaCacheKey -> {
      String[] path = schemaCacheKey.split(SEPARATOR);
      Action action = api.getResource(path[0]).getAction(path[1]);
      return action.getBody().get(path[2]);
    };

    jsonSchemaCache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .build(new JsonSchemaCacheLoader(api, keyBuilder));

    xmlSchemaCache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .build(new XmlSchemaCacheLoader(api, keyBuilder));
  }

  public JsonSchema resolveJsonSchema(Action action, String requestMimeTypeName) {
    String key = getSchemaCacheKey(action, requestMimeTypeName);
    try {
      return jsonSchemaCache.get(key);
    } catch (ExecutionException e) {
      throw new RuntimeException("Json Schema could not be resolved for key: " + key);
    }
  }

  public Schema resolveXmlSchema(Action action, String requestMimeTypeName) {
    String key = getSchemaCacheKey(action, requestMimeTypeName);
    try {
      return xmlSchemaCache.get(key);
    } catch (ExecutionException e) {
      throw new RuntimeException("XML Schema could not be resolved for key: " + key);
    }
  }

  private String getSchemaCacheKey(Action action, String mimeTypeName) {
    StringBuilder key = new StringBuilder(action.getResource().getUri());
    key.append(SEPARATOR).append(action.getType());
    key.append(SEPARATOR).append(mimeTypeName);
    return key.toString();
  }

}
