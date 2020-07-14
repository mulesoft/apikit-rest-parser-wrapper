/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.apikit.implv1.model.parameter;

import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.implv1.ParserWrapperV1;
import org.mule.apikit.implv1.model.RamlImplV1;
import org.mule.apikit.implv1.model.ResourceImpl;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.metadata.api.annotation.EnumAnnotation;
import org.mule.metadata.api.annotation.IntAnnotation;
import org.mule.metadata.api.model.MetadataType;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ParameterImplTest {

  private static final String EXAMPLE = "BAR";
  private static final String JAVA = "java";
  private static final String STRING_PARAM_INVALID_EXAMPLE = "CARP";
  private static final String STRING_PARAM_EXAMPLE = "RIV";
  private RamlImplV1 api;
  private ParameterImpl stringParameter;

  @Before
  public void setUp() throws Exception {
    String apiLocation = this.getClass().getResource("/apis/08-leagues/api.raml").toURI().toString();
    api = (RamlImplV1) new ParserWrapperV1(apiLocation, Collections.emptyList()).parse();
    ResourceImpl resource = (ResourceImpl) api.getResource("/teams").getResources().get("/{teamId}");
    stringParameter = (ParameterImpl) resource.getResolvedUriParameters().get("teamId");
  }

  @Test
  public void isRequiredTest() {
    assertFalse(stringParameter.isRequired());
  }

  @Test
  public void getDefaultValueTest() {
    assertNull(stringParameter.getDefaultValue());
  }

  @Test
  public void isRepeatTest() {
    assertFalse(stringParameter.isRepeat());
  }

  @Test
  public void isArrayTest() {
    assertFalse(stringParameter.isArray());
  }

  @Test
  public void validateTest() {
    assertFalse(stringParameter.validate(STRING_PARAM_INVALID_EXAMPLE));
    assertTrue(stringParameter.validate(STRING_PARAM_EXAMPLE));
  }

  @Test
  public void messageTest() {
    assertEquals("Value length is longer than 3", stringParameter.message(STRING_PARAM_INVALID_EXAMPLE));
    assertEquals("OK", stringParameter.message(STRING_PARAM_EXAMPLE));
  }

  @Test
  public void getDisplayNameTest() {
    assertNull(stringParameter.getDisplayName());
  }

  @Test
  public void getDescriptionTest() {
    assertEquals("Three letter code that identifies the team.\n", stringParameter.getDescription());
  }

  @Test
  public void getExampleTest() {
    assertEquals(EXAMPLE, stringParameter.getExample());
  }

  @Test
  public void getExamplesTest() {
    assertEquals(0, stringParameter.getExamples().size());
  }

  @Test
  public void getInstanceTest() {
    assertNotNull(stringParameter.getInstance());
  }

  @Test
  public void getMetadataTest() {
    assertEquals(JAVA, stringParameter.getMetadata().getMetadataFormat().getId());

    ResourceImpl resource = (ResourceImpl) api.getResource("/history/{version}");
    ParameterImpl parameter = (ParameterImpl) resource.getBaseUriParameters().get("apiDomain").get(0);
    MetadataType metadata = parameter.getMetadata();
    assertNotNull(metadata.getAnnotation(EnumAnnotation.class).orElse(null));
    assertEquals(JAVA, metadata.getMetadataFormat().getId());

    resource = (ResourceImpl) resource.getResources().get("/{year}");
    parameter = (ParameterImpl) resource.getResolvedUriParameters().get("year");
    metadata = parameter.getMetadata();
    assertNotNull(metadata.getAnnotation(IntAnnotation.class).orElse(null));
    assertEquals(JAVA, metadata.getMetadataFormat().getId());

    Map<String, Parameter> queryParams = resource.getAction("GET").getQueryParameters();
    parameter = (ParameterImpl) queryParams.get("includeStatistics");
    metadata = parameter.getMetadata();
    assertTrue(metadata.getAnnotations().isEmpty());
    assertEquals(JAVA, metadata.getMetadataFormat().getId());

    parameter = (ParameterImpl) queryParams.get("minScoreRate");
    metadata = parameter.getMetadata();
    assertTrue(metadata.getAnnotations().isEmpty());
    assertEquals(JAVA, metadata.getMetadataFormat().getId());
  }

  @Test
  public void isScalarTest() {
    assertTrue(stringParameter.isScalar());
  }

  @Test
  public void isFacetArrayTest() {
    assertFalse(stringParameter.isFacetArray("String"));
  }

  @Test
  public void surroundWithQuotesIfNeededTest() {
    assertEquals(EXAMPLE, stringParameter.surroundWithQuotesIfNeeded(EXAMPLE));
  }
}
