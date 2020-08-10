/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.apikit.implv1.model;

import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.common.LazyValue;
import org.mule.apikit.implv1.ParserWrapperV1;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MimeTypeImplTest {

  private static final String APPLICATION_JSON = "application/json";
  private static final String MULTIPART_FORM_DATA = "multipart/form-data";
  private static final String ACTION_POST = "POST";
  private static final String ACTION_PUT = "PUT";

  private static final String EXAMPLE = "          {\n" +
      "            \"name\": \"Barcelona\",\n" +
      "            \"id\": \"BAR\",\n" +
      "            \"homeCity\": \"Barcelona\",\n" +
      "            \"stadium\": \"Camp Nou\"\n" +
      "          }";


  private MimeTypeImpl jsonMimeType;
  private MimeTypeImpl formMimeType;

  @Before
  public void setUp() throws Exception {
    String apiLocation = this.getClass().getResource("/apis/08-leagues/api.raml").toURI().toString();
    RamlImplV1 api = (RamlImplV1) new ParserWrapperV1(apiLocation, new LazyValue<>(Collections::emptyList)).parse();
    jsonMimeType = (MimeTypeImpl) api.getResource("/teams").getAction(ACTION_POST).getBody().get(APPLICATION_JSON);
    formMimeType = (MimeTypeImpl) api.getResource("/badge").getAction(ACTION_PUT).getBody().get(MULTIPART_FORM_DATA);
  }


  @Test
  public void getCompiledSchemaTest() {
    assertTrue(jsonMimeType.getCompiledSchema().toString().endsWith("apis/08-leagues/schemas/teams-schema-output.json"));
  }

  @Test
  public void getSchemaTest() {
    assertTrue(jsonMimeType.getSchema().contains("Name of the city to which this team belongs"));
  }

  @Test
  public void getFormParametersTest() {
    assertNull(jsonMimeType.getFormParameters());
    assertEquals(2, formMimeType.getFormParameters().size());
  }

  @Test
  public void getTypeTest() {
    assertEquals(APPLICATION_JSON, jsonMimeType.getType());

  }

  @Test
  public void getExampleTest() {
    assertTrue(jsonMimeType.getExample().contains("Camp Nou"));
  }

  @Test
  public void getInstanceTest() {
    assertNotNull(jsonMimeType.getInstance());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void validateTest() {
    jsonMimeType.validate(EXAMPLE);
  }
}
