/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.schemas;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.cache.CacheLoader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.MimeType;
import org.mule.validation.uri.URICoder;

public class JsonSchemaCacheLoader extends CacheLoader<String, JsonSchema> {

  private final ApiSpecification api;
  private static final String RESOURCE_PREFIX = "resource:/";
  private final Function<String, MimeType> keyBuilder;

  public JsonSchemaCacheLoader(ApiSpecification api,
      Function<String, MimeType> keyBuilder) {
    this.api = api;
    this.keyBuilder = keyBuilder;
  }

  @Override
  public JsonSchema load(String schemaLocation) {
    Object pathOrSchema = resolveJsonSchema(schemaLocation);

    if (pathOrSchema == null) {
      return null;
    }

    if (pathOrSchema instanceof String) {
      return parseSchema(resolveLocationIfNecessary(formatUri((String) pathOrSchema)));
    }

    return parseSchema((JsonNode) pathOrSchema);
  }

  private Object resolveJsonSchema(String schemaCacheKey) {
    MimeType mimeType = keyBuilder.apply(schemaCacheKey);
    String path = (String) mimeType.getCompiledSchema();
    String schemaOrGlobalReference = mimeType.getSchema();

    try {
      //check global schemas
      if (api.getConsolidatedSchemas().containsKey(schemaOrGlobalReference)) {
        path = (String) api.getCompiledSchemas().get(schemaOrGlobalReference);
        if (path != null) {
          return path;
        }
        return JsonLoader.fromString(api.getConsolidatedSchemas().get(schemaOrGlobalReference));
      }

      if (path != null) {
        return path;
      }

      if (schemaOrGlobalReference != null) {
        return JsonLoader.fromString(schemaOrGlobalReference);
      }
      return null;

    } catch (IOException e) {
      throw new RuntimeException("Json Schema could not be resolved for key: " + schemaCacheKey, e);
    }
  }

  /*
   * make the location json schema validator friendly appending
   *  resource:/ if necessary
   */
  private String formatUri(String location) {
    String encodedLocation = getEncodedPath(location);
    URI uri = URI.create(encodedLocation);

    if (uri.getScheme() == null) {
      if (location.charAt(0) == '/') {
        location = location.substring(1);
      }

      location = RESOURCE_PREFIX + location;
    }

    return location;
  }

  /*
   * in order to find the resource in the application classpath
   *  the resource:/ url is translated to a file:/ url
   */
  private String resolveLocationIfNecessary(String path) {
    String encodedUri = getEncodedPath(path);
    URI uri = URI.create(encodedUri);

    String scheme = uri.getScheme();
    if (scheme == null || "resource".equals(scheme)) {
      return openSchema(uri.getPath()).toString();
    }
    return path;
  }

  private String getEncodedPath(String path) {
    Set<Character> ignoredCharacters = new HashSet<>();
    ignoredCharacters.add('/');
    ignoredCharacters.add(':');

    return URICoder.encode(path, ignoredCharacters);
  }

  private URL openSchema(String path) {
    try {
      return Paths.get(path).toUri().toURL();
    } catch (MalformedURLException e) {
      return null;
    }
  }

  private JsonSchema parseSchema(JsonNode jsonNode) {
    try {
      return getSchemaFactory().getJsonSchema(jsonNode);
    } catch (ProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private JsonSchema parseSchema(String uri) {
    try {
      // Uri might be previously encoded. In order to avoid encoding the relative part again,
      // we previously decode the it, and then we encode the absolute Uri again
      String decodedURI = URICoder.decode(uri);
      return getSchemaFactory().getJsonSchema(getEncodedPath(decodedURI));
    } catch (ProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private JsonSchemaFactory getSchemaFactory() {
    ValidationConfiguration validationCfg =
        ValidationConfiguration.newBuilder().setDefaultVersion(SchemaVersion.DRAFTV3).freeze();
    return JsonSchemaFactory.newBuilder().setLoadingConfiguration(LoadingConfiguration.byDefault())
        .setValidationConfiguration(validationCfg).freeze();
  }
}
