/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import org.junit.Before;
import org.junit.Test;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.api.ApiReference;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Oas20PayloadWithoutMimeTypeValidationTestCase {

  private static final String VALID_XML = "<User><id>1</id><name>name</name></User>";
  private static final String INVALID_XML = "<User><id1>1</id1><name>name</name></User>";
  private static final String VALID_JSON = "{\"id\":1, \"name\":\"test\"}";
  private static final String INVALID_JSON = "{\"id1\":1, \"name\":\"test\"}";
  private ApiSpecification oas20ApiSpecification;

  @Before
  public void setUp() throws Exception {
    String apiLocation = Thread.currentThread().getContextClassLoader()
        .getResource("org/mule/amf/impl/oas20-payload-without-mimetype/oas20-payload-without-mimetype.yaml").toURI().toString();
    ApiReference oas20apiRef = ApiReference.create(apiLocation);
    oas20ApiSpecification = new AMFParser(oas20apiRef).parse();
  }


  @Test
  public void withSchemaAndWithoutMimeTypeTest() throws Exception {
    Map<String, MimeType> bodies = oas20ApiSpecification.getResource("/withSchema").getAction("POST").getBody();
    MimeType xmlMimeType = bodies.get("application/xml");
    MimeType jsonMimeType = bodies.get("application/json");

    assertTrue(xmlMimeType.validate(VALID_XML).isEmpty());
    assertFalse(xmlMimeType.validate(INVALID_XML).isEmpty());
    assertTrue(jsonMimeType.validate(VALID_JSON).isEmpty());
    assertFalse(jsonMimeType.validate(INVALID_JSON).isEmpty());
  }

  @Test
  public void withoutSchemaAndWithoutMimeTypeTest() throws Exception {
    Map<String, MimeType> bodies = oas20ApiSpecification.getResource("/withoutSchema").getAction("POST").getBody();
    assertTrue(bodies.isEmpty());
  }
}
