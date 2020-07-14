/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.validation.ApiValidationReport;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ApiParserAmfTestCase {

  private AMFParser amfApiParser;

  @Before
  public void setup() throws Exception {
    String pathAsUri =
        requireNonNull(getClass().getClassLoader().getResource("org/mule/amf/impl/ref-json-schema/input.raml")).toString();
    amfApiParser = new AMFParser(ApiReference.create(pathAsUri), true);
  }

  @Test
  public void testGetAllReferences() {
    List<String> references = amfApiParser.parse().getAllReferences();

    assertThat(references.size(), is(2));
    assertThat(references.stream().anyMatch(ref -> ref.endsWith("org/mule/amf/impl/ref-json-schema/car-schema.json")), is(true));
    assertThat(references.stream().anyMatch(ref -> ref.endsWith("org/mule/amf/impl/ref-json-schema/car-properties-schema.json")),
               is(true));
  }

  @Test
  public void testRAML10ApiValidationReport() {
    ApiValidationReport report = amfApiParser.validate();
    assertNotNull(report);
    assertTrue(report.conforms());
  }

}
