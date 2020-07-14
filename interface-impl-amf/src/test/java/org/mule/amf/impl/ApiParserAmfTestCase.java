/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.validation.ApiValidationReport;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ApiParserAmfTestCase {

  private static final String USER_RESOURCE = "/user";
  private static final String CAR_RESOURCE = "/car";
  private static final String GET_ACTION = "GET";
  private static final String POST_ACTION = "POST";
  private static final String APPLICATION_JSON = "application/json";
  private static final String STATUS_200 = "200";

  @Parameterized.Parameter
  public ApiVendor apiVendor;

  @Parameterized.Parameter(1)
  public AMFParser amfApiParser;

  @Parameterized.Parameters(name = "{0}")
  public static Collection apiSpecifications() throws Exception {
    String apiLocation = ApiParserAmfTestCase.class.getResource("references/api.raml").toURI().toString();
    ApiReference ramlApiRef = ApiReference.create(apiLocation);

    apiLocation = ApiParserAmfTestCase.class.getResource("references/oas20-api.yaml").toURI().toString();
    ApiReference oas20apiRef = ApiReference.create(apiLocation);

    apiLocation = ApiParserAmfTestCase.class.getResource("references/oas30-api.yaml").toURI().toString();
    ApiReference oas30apiRef = ApiReference.create(apiLocation);

    return Arrays.asList(new Object[][] {
        {ApiVendor.RAML, new AMFParser(ramlApiRef, false)},
        {ApiVendor.OAS_20, new AMFParser(oas20apiRef, false)},
        {ApiVendor.OAS_30, new AMFParser(oas30apiRef, false)}
    });
  }

  @Test
  public void testGetAllReferences() {
    List<String> references = amfApiParser.parse().getAllReferences();

    if (ApiVendor.RAML.equals(apiVendor)) {
      assertEquals(9, references.size());
    } else if (ApiVendor.OAS_20.equals(apiVendor)) {
      assertEquals(4, references.size());
    } else if (ApiVendor.OAS_30.equals(apiVendor)) {
      assertEquals(5, references.size());
    }
    assertTrue(references.stream()
        .anyMatch(ref -> ref.endsWith("org/mule/amf/impl/references/car-schemas/car-properties-schema.json")));
    if (ApiVendor.RAML.equals(apiVendor)) {
      assertTrue(references.stream().anyMatch(ref -> ref.endsWith("org/mule/amf/impl/references/person-schema.json")));
      assertTrue(references.stream()
          .anyMatch(ref -> ref.endsWith("org/mule/amf/impl/references/car-schemas/manufacturer-schema.json")));
      assertTrue(references.stream().anyMatch(ref -> ref.endsWith("org/mule/amf/impl/references/car-schema.json")));
      assertTrue(references.stream()
          .anyMatch(ref -> ref.endsWith("org/mule/amf/impl/references/examples/user-example-for-raml.json")));
      // AMF does not support the externalValue facet of examples as a reference for the time being. They are treated as a string.
      // ExternalValue facet is not supported for OAS 20
      // TODO the following two assertions should be done for OAS30 once externalValue facet is supported.
      assertTrue(references.stream().anyMatch(ref -> ref.endsWith("org/mule/amf/impl/references/examples/car-example.json")));
      assertTrue(references.stream().anyMatch(ref -> ref.endsWith("org/mule/amf/impl/references/examples/user-example.json")));
    } else {
      assertTrue(references.stream()
          .anyMatch(ref -> ref.endsWith("org/mule/amf/impl/references/person-schemas/phone-schema.yml")));
      assertTrue(references.stream()
          .anyMatch(ref -> ref.endsWith("org/mule/amf/impl/references/person-schemas/address-schema.yaml")));
      assertTrue(references.stream().anyMatch(ref -> ref.endsWith("org/mule/amf/impl/references/person-schema.yaml")));
    }
    if (ApiVendor.OAS_30.equals(apiVendor)) {
      assertTrue(references.stream()
          .anyMatch(ref -> ref.endsWith("org/mule/amf/impl/references/examples/user-example-for-oas30.json")));
    }
  }

  @Test
  public void testApiValidationReport() {
    ApiValidationReport report = amfApiParser.validate();
    assertNotNull(report);
    assertTrue(report.conforms());
  }

  @Test
  public void testReferencedExamples() {
    ApiSpecification api = amfApiParser.parse();
    Response response = api.getResource(USER_RESOURCE).getAction(GET_ACTION).getResponses().get(STATUS_200);
    Map<String, String> response_examples = response.getExamples();
    assertNotNull(response_examples);
    assertTrue(response_examples.size() > 0);
    assertNotNull(response.getBody().get(APPLICATION_JSON).getExample());
    if (ApiVendor.RAML.equals(apiVendor)) {
      // AMF is currently treating the externalValue facet of examples as a string. Not supported for OAS 20
      assertNotNull(api.getResource(CAR_RESOURCE).getAction(POST_ACTION).getBody().get(APPLICATION_JSON).getExample());
    }
  }

}
