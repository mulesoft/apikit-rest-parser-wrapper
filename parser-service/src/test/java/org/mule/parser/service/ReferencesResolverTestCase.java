/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import java.io.InputStream;
import java.util.Optional;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.references.ReferencesResolver;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ReferencesResolverTestCase {

  @Test
  public void amfThrowExceptionParsingApiReference() {
    ReferencesResolver referencesResolver = new ReferencesResolver();
    assertThat(referencesResolver.getReferences(new ApiReference() {

      @Override
      public String getLocation() {
        throw new RuntimeException("Testing exception");
      }

      @Override
      public String getFormat() {
        throw new RuntimeException("Testing exception");
      }

      @Override
      public InputStream resolve() {
        throw new RuntimeException("Testing exception");
      }

      @Override
      public Optional<ResourceLoader> getResourceLoader() {
        throw new RuntimeException("Testing exception");
      }
    }), equalTo(emptyList()));
  }
}
