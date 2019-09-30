/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.parser.visitor;

import org.mule.apikit.implv1.loader.SnifferResourceLoader;
import org.mule.apikit.implv1.model.RamlImplV1;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.visitor.ApiDocumentBuilder;

import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.loader.DefaultResourceLoader;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;

public class RamlDocumentBuilderImpl implements ApiDocumentBuilder {

  private RamlDocumentBuilder ramlDocumentBuilder;
  private SnifferResourceLoader snifferResourceLoader;

  public RamlDocumentBuilderImpl(String resource, ResourceLoader resourceLoader) {
    snifferResourceLoader = new SnifferResourceLoader(resourceLoader, resource);
    ramlDocumentBuilder = new RamlDocumentBuilder(snifferResourceLoader);
  }

  public RamlDocumentBuilderImpl() {
    snifferResourceLoader = new SnifferResourceLoader(new DefaultResourceLoader(), null);
    ramlDocumentBuilder = new RamlDocumentBuilder(snifferResourceLoader);
  }

  public ApiSpecification build(String content, String resourceLocation) {
    return new RamlImplV1(ramlDocumentBuilder.build(content, resourceLocation), snifferResourceLoader, resourceLocation);
  }

  public ApiSpecification build(String resourceLocation) {
    return new RamlImplV1(ramlDocumentBuilder.build(resourceLocation), snifferResourceLoader, resourceLocation);
  }

  public ApiDocumentBuilder addPathLookupFirst(String path) {
    if (path != null) {
      ResourceLoader loader = ramlDocumentBuilder.getResourceLoader();
      loader = new CompositeResourceLoader(new FileResourceLoader(path), loader);
      ramlDocumentBuilder = new RamlDocumentBuilder(loader);
    }
    return this;

  }

  public RamlDocumentBuilderImpl getInstance() {
    return this;
  }

  public ResourceLoader getResourceLoader() {
    return ramlDocumentBuilder.getResourceLoader();
  }
}
