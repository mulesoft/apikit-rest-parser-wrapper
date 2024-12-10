/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation;

import static java.lang.String.format;
import static org.mule.validation.uri.URIResolver.MatchRule.BEST_MATCH;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.mule.validation.exception.ResourceNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javafx.util.Pair;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.Resource;
import org.mule.validation.uri.ResolvedVariables;
import org.mule.validation.uri.URIPattern;
import org.mule.validation.uri.URIResolver;

public class ResourceFinder {

  private static final int URI_CACHE_SIZE = 1000;
  private final Map<URIPattern, Resource> mappings = new HashMap<>();
  private final LoadingCache<String, URIResolver> uriResolverCache;
  private final LoadingCache<String, URIPattern> uriPatternCache;

  public ResourceFinder(ApiSpecification apiSpecification) {
    this.buildMappings(apiSpecification.getResources().values(), apiSpecification.getVersion());

    uriResolverCache = CacheBuilder.newBuilder()
        .maximumSize(URI_CACHE_SIZE)
        .build(
            new CacheLoader<String, URIResolver>() {
              @Override
              public URIResolver load(String path) {
                return new URIResolver(path);
              }
            });

    uriPatternCache = CacheBuilder.newBuilder()
        .maximumSize(URI_CACHE_SIZE)
        .build(
            new CacheLoader<String, URIPattern>() {
              @Override
              public URIPattern load(String path) throws Exception {
                URIResolver resolver = uriResolverCache.get(path);
                URIPattern match = resolver.find(mappings.keySet(), BEST_MATCH);
                if (match == null) {
                  throw new ResourceNotFoundException("No matching patterns for URI " + path);
                }
                return match;
              }
            });

  }

  private void buildMappings(Collection<Resource> resources, String version) {
    for (Resource resource : resources) {
      mappings.put(new URIPattern(resource.getResolvedUri(version)), resource);
      if (resource.getResources() != null) {
        buildMappings(resource.getResources().values(), version);
      }
    }
  }

  /**
   * find an Action using http request method and uri
   * @param method
   * @param uri
   * @return the Action and a map with uri params values
   * @throws ResourceNotFoundException
   */
  public Pair<Action, Map<String,String>> findAction(String method, String uri) throws ResourceNotFoundException {
    try {
      URIPattern uriPattern = uriPatternCache.get(uri);
      URIResolver resolver = uriResolverCache.get(uri);
      ResolvedVariables resolvedVariables = resolver.resolve(uriPattern);

      Resource resource = mappings.get(uriPattern);
      if (resource == null) {
        throw new ResourceNotFoundException(format("Resource not found for : %s", uri));
      }

      Action action = resource.getAction(method.toLowerCase());
      if (action == null) {
        // TODO : method not allowed exception
        throw new ResourceNotFoundException(
            format("Action not found for %s and method %s", uri, method));
      }

      return new Pair<>(action, resolvedVariablesToMap(resolvedVariables));
    } catch (Exception e) {
      throw new ResourceNotFoundException(e.getMessage());
    }
  }

  private Map<String, String> resolvedVariablesToMap(ResolvedVariables resolvedVariables) {
    Map<String, String> uriParams = new HashMap<>();
    resolvedVariables.names().stream().forEach(variable -> uriParams.put(variable, (String)resolvedVariables.get(variable)));
    return uriParams;
  }

}
