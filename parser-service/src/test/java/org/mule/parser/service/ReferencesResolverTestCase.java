/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import org.junit.Test;
import org.mockito.Mockito;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.references.ReferencesResolver;
import org.mule.parser.service.strategy.AMFParsingStrategy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mule.apikit.model.api.ApiReference.create;

public class ReferencesResolverTestCase {

  @Test
  public void amfThrowExceptionParsingApiReference() throws Exception {
    AMFParsingStrategy parsingStrategy = Mockito.mock(AMFParsingStrategy.class);
    when(parsingStrategy.parse(any(ApiReference.class))).thenThrow(new RuntimeException("Testing exception"));

    setFinalStatic(ReferencesResolver.class.getDeclaredField("amfParsingStrategy"), parsingStrategy);

    ReferencesResolver referencesResolver = new ReferencesResolver();

    assertThat(referencesResolver.getReferences(create("path/to/api.raml")), equalTo(emptyList()));
  }

  private void setFinalStatic(Field field, Object newValue) throws Exception {
    field.setAccessible(true);
    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    field.set(null, newValue);
  }

}
