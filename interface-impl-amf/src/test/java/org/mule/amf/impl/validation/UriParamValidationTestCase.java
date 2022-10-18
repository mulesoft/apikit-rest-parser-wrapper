/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.validation;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.amf.impl.AMFParser;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.model.parameter.Parameter;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UriParamValidationTestCase {

  private UriParamTestBuilder uriParamTestBuilder;


  @Before
  public void setUp() {
    this.uriParamTestBuilder = new UriParamTestBuilder("/org/mule/amf/impl/validation/api.yaml");
  }

  @Test
  public void OASObjectDefaultsUriParamTest() throws Exception {
    UriParamTest oasObjectSimpleUriParam = uriParamTestBuilder
        .withResource("/uri-parameters-object/{object-value}")
        .withParam("object-value")
        .build();

    oasObjectSimpleUriParam.validate("firstname,juan,lastname,brasca,age,22");
  }

  @Test
  public void OASObjectSimpleUriParamTest() throws Exception {
    UriParamTest objectSimpleUriParamTest = uriParamTestBuilder
        .withResource("/uri-parameters-simple-object/{object-value}")
        .withParam("object-value")
        .build();

    objectSimpleUriParamTest.validate("firstname,juan,lastname,brasca,age,22");
  }

  @Test
  public void OASScalarArrayUriParamTest() throws Exception {
    UriParamTest oasScalarArrayUriParamTest = uriParamTestBuilder
        .withResource("/uri-parameters-scalar-array/{scalar-array}")
        .withParam("scalar-array")
        .build();

    oasScalarArrayUriParamTest.validate("1,2,3");
  }

  @Test
  public void OASScalarUriParamTest() throws Exception {
    UriParamTest oasScalarUriParamTest = uriParamTestBuilder
        .withResource("/uri-parameters-scalar/{scalar}")
        .withParam("scalar")
        .build();

    oasScalarUriParamTest.validate("5");
  }

  @Ignore("Out of scope for the task")
  @Test
  public void OASUnionOfObjectsUriParamTest() throws Exception {
    UriParamTest oasUnionOfObjectsUriParamTest = uriParamTestBuilder
        .withResource("/uri-parameters-object-union/{object-value}")
        .withParam("object-value")
        .build();

    oasUnionOfObjectsUriParamTest.validate("firstname,juan,lastname,brasca,age,22");
  }

  @Test
  public void OASObjectAsJsonUriParamTest() throws Exception {
    UriParamTest oasObjectAsJsonUriParamTest = uriParamTestBuilder
        .withResource("/uri-parameters-json-object/{object-value}")
        .withParam("object-value")
        .build();

    oasObjectAsJsonUriParamTest.validate("{\"firstname\": \"juan\",  \"lastname\": \"brasca\",  \"age\": 22}");
  }

  protected class UriParamTestBuilder {

    String path;
    String resource;
    String method = "GET";
    String param;

    public UriParamTestBuilder(String path, String resource, String param) {
      this.path = path;
      this.resource = resource;
      this.param = param;
    }

    public UriParamTestBuilder(String path) {
      this.path = path;
    }

    public UriParamTestBuilder withResource(String resource) {
      this.resource = resource;
      return this;
    }

    public UriParamTestBuilder withParam(String param) {
      this.param = param;
      return this;
    }


    public UriParamTest build() throws Exception {
      validateMandatory();
      String apiLocation = UriParamValidationTestCase.class.getResource(path).toURI().toString();
      ApiReference oas30apiRef = ApiReference.create(apiLocation);
      AMFParser parser = new AMFParser(oas30apiRef);
      Map<String, Resource> resources = parser.parse().getResources();
      Parameter parameter = resources.get(resource).getAction(method).getResolvedUriParameters().get(param);

      return new UriParamTest(parameter);

    }

    private void validateMandatory() {
      assertNotNull(resource);
      assertNotNull(param);
    }
  }

  protected class UriParamTest {

    private final Parameter parameter;

    public UriParamTest(Parameter parameter) {
      this.parameter = parameter;
    }

    UriParamTest validate(String paramValue) {
      assertTrue(parameter.validate(paramValue));
      return this;
    }
  }
}
