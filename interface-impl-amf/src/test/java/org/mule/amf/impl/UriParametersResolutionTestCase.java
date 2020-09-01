/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.model.parameter.Parameter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mule.apikit.model.ActionType.GET;
import static org.mule.apikit.model.ActionType.PUT;
import static org.mule.apikit.model.ApiVendor.OAS_20;
import static org.mule.apikit.model.ApiVendor.OAS_30;
import static org.mule.apikit.model.ApiVendor.RAML_08;
import static org.mule.apikit.model.ApiVendor.RAML_10;

@RunWith(Parameterized.class)
public class UriParametersResolutionTestCase {

  @Parameterized.Parameter
  public ApiVendor apiVendor;

  @Parameterized.Parameter(1)
  public AMFParser amfApiParser;

  private Map<String, Resource> resources;

  @Parameterized.Parameters(name = "{0}")
  public static Collection apiSpecifications() throws Exception {
    String apiLocation = UriParametersResolutionTestCase.class.getResource("uri-parameters/raml08/api.raml").toURI().toString();
    ApiReference raml08ApiRef = ApiReference.create(apiLocation);

    apiLocation = UriParametersResolutionTestCase.class.getResource("uri-parameters/raml10/api.raml").toURI().toString();
    ApiReference raml10ApiRef = ApiReference.create(apiLocation);

    apiLocation = UriParametersResolutionTestCase.class.getResource("uri-parameters/oas20/api.yaml").toURI().toString();
    ApiReference oas20apiRef = ApiReference.create(apiLocation);

    apiLocation = UriParametersResolutionTestCase.class.getResource("uri-parameters/oas30/api.yaml").toURI().toString();
    ApiReference oas30apiRef = ApiReference.create(apiLocation);

    return Arrays.asList(new Object[][] {
        {RAML_08, new AMFParser(raml08ApiRef)},
        {RAML_10, new AMFParser(raml10ApiRef)},
        {OAS_20, new AMFParser(oas20apiRef)},
        {OAS_30, new AMFParser(oas30apiRef)}
    });
  }

  @Before
  public void setup() {
    resources = amfApiParser.parse().getResources();
  }

  @Test
  public void noUriParamsAtAll() {
    // RESOURCE
    Resource resource = resources.get("/noUriParamsOverridden");
    // ACTION: GET
    assertUriParamsFound(resource.getAction(GET.name()).getResolvedUriParameters(), 0);
    // ACTION: PUT
    assertUriParamsFound(resource.getAction(PUT.name()).getResolvedUriParameters(), 0);
  }

  @Test
  public void uriParamsNotOverriddenInResourceOrActionButRequired() {
    assumeTrue(RAML_08.equals(apiVendor) || RAML_10.equals(apiVendor));
    // RESOURCE
    Resource resource = resources.get("/noUriParamsButRequired/{testParam}");
    // ACTION: GET
    assertUriParamsFound(resource.getAction(GET.name()).getResolvedUriParameters(), 1);
    // ACTION: PUT
    assertUriParamsFound(resource.getAction(PUT.name()).getResolvedUriParameters(), 1);
  }

  @Test
  public void uriParamsOverriddenInResourceButNotInMethods() {
    // RESOURCE
    Resource resource = resources.get("/templateUriParamsInResource/{testParam}");
    // ACTION: GET
    Map<String, Parameter> resolvedUriParameters = resource.getAction(GET.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 1);
    assertTrue(resolvedUriParameters.get("testParam").validate("resource"));
    // ACTION: PUT
    resolvedUriParameters = resource.getAction(PUT.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 1);
    assertTrue(resolvedUriParameters.get("testParam").validate("resource"));
  }

  @Test
  public void baseUriParamsOverriddenInResourceButNotInMethods() {
    assumeTrue(RAML_08.equals(apiVendor) || RAML_10.equals(apiVendor));
    // RESOURCE
    Resource resource = resources.get("/baseUriParamsOverriddenInResource/{testParam}");
    // ACTION: GET
    Map<String, Parameter> resolvedUriParameters = resource.getAction(GET.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 2);
    assertTrue(resolvedUriParameters.get("apiDomain").validate("resource-domain"));
    // ACTION: PUT
    resolvedUriParameters = resource.getAction(PUT.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 2);
    assertTrue(resolvedUriParameters.get("apiDomain").validate("resource-domain"));
  }

  @Test
  public void baseUriParamOverriddenInMethodsOnly() {
    assumeTrue(RAML_08.equals(apiVendor));
    // RESOURCE
    Resource resource = resources.get("/baseUriParamsOverriddenInMethods/{testParam}");
    // ACTION: GET
    Map<String, Parameter> resolvedUriParameters = resource.getAction(GET.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 2);
    assertTrue(resolvedUriParameters.get("apiDomain").validate("domain-get"));
    // ACTION: PUT
    resolvedUriParameters = resource.getAction(PUT.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 2);
    assertTrue(resolvedUriParameters.get("apiDomain").validate("domain-put"));
  }

  @Test
  public void baseUriParamOverriddenInResourceAndInMethods() {
    assumeTrue(RAML_08.equals(apiVendor));
    // RESOURCE
    Resource resource = resources.get("/baseUriParamsOverriddenInResourceAndMethods/{testParam}");
    // ACTION: GET
    Map<String, Parameter> resolvedUriParameters = resource.getAction(GET.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 2);
    assertTrue(resolvedUriParameters.get("apiDomain").validate("domain-get"));
    // ACTION: PUT
    resolvedUriParameters = resource.getAction(PUT.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 2);
    assertTrue(resolvedUriParameters.get("apiDomain").validate("domain-put"));
  }

  @Test
  public void baseUriParamOverriddenInOneMethodOnly() {
    assumeTrue(RAML_08.equals(apiVendor));
    // RESOURCE
    Resource resource = resources.get("/baseUriParamsOverriddenInOneMethod/{testParam}");
    // ACTION: GET
    Map<String, Parameter> resolvedUriParameters = resource.getAction(GET.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 2);
    assertTrue(resolvedUriParameters.get("apiDomain").validate("domain-get"));
    // ACTION: PUT
    resolvedUriParameters = resource.getAction(PUT.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 1);
    assertTrue(resolvedUriParameters.get("testParam").validate("resource"));
  }

  @Test
  public void baseUriParamOverriddenInResourceAndInOneMethod() {
    assumeTrue(RAML_08.equals(apiVendor));
    // RESOURCE
    Resource resource = resources.get("/baseUriParamsOverriddenInResourceAndOneMethod/{testParam}");
    // ACTION: GET
    Map<String, Parameter> resolvedUriParameters = resource.getAction(GET.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 2);
    assertTrue(resolvedUriParameters.get("testParam").validate("resource"));
    assertTrue(resolvedUriParameters.get("apiDomain").validate("resource-domain"));
    // ACTION: PUT
    resolvedUriParameters = resource.getAction(PUT.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 2);
    assertTrue(resolvedUriParameters.get("testParam").validate("resource"));
    assertTrue(resolvedUriParameters.get("apiDomain").validate("domain-put"));
  }

  @Test
  public void templateUriParamsInMethodsOnly() {
    assumeTrue(OAS_20.equals(apiVendor) || OAS_30.equals(apiVendor));
    // RESOURCE
    Resource resource = resources.get("/templateUriParamsInMethods/{testParam}");
    // ACTION: GET
    Map<String, Parameter> resolvedUriParameters = resource.getAction(GET.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 1);
    assertTrue(resolvedUriParameters.get("testParam").validate("method-get"));
    // ACTION: PUT
    resolvedUriParameters = resource.getAction(PUT.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 1);
    assertTrue(resolvedUriParameters.get("testParam").validate("method-put"));
  }

  @Test
  public void templateUriParamsInResourceAndMethods() {
    assumeTrue(OAS_20.equals(apiVendor) || OAS_30.equals(apiVendor));
    // RESOURCE
    Resource resource = resources.get("/templateUriParamsInResourceAndMethods/{testParam}");
    // ACTION: GET
    Map<String, Parameter> resolvedUriParameters = resource.getAction(GET.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 1);
    assertTrue(resolvedUriParameters.get("testParam").validate("method-get"));
    // ACTION: PUT
    resolvedUriParameters = resource.getAction(PUT.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 1);
    assertTrue(resolvedUriParameters.get("testParam").validate("method-put"));
  }

  @Test
  public void templateUriParamsInResourceOverriddenInOneMethod() {
    assumeTrue(OAS_20.equals(apiVendor) || OAS_30.equals(apiVendor));
    // RESOURCE
    Resource resource = resources.get("/templateUriParamsInResourceOverriddenInOneMethod/{testParam}");
    // ACTION: GET
    Map<String, Parameter> resolvedUriParameters = resource.getAction(GET.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 1);
    assertTrue(resolvedUriParameters.get("testParam").validate("resource"));
    // ACTION: PUT
    resolvedUriParameters = resource.getAction(PUT.name()).getResolvedUriParameters();
    assertUriParamsFound(resolvedUriParameters, 1);
    assertTrue(resolvedUriParameters.get("testParam").validate("method-put"));
  }

  private void assertUriParamsFound(Map<String, Parameter> resolvedUriParameters, int expectedCount,
                                    String... expectedUriParams) {
    assertNotNull(resolvedUriParameters);
    assertEquals(expectedCount, resolvedUriParameters.size());
    for (String expectedUriParam : expectedUriParams) {
      assertNotNull(resolvedUriParameters.get(expectedUriParam));
    }
  }
}
