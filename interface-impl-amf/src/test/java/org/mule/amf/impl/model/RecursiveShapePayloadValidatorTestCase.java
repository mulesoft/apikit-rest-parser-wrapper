/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mule.amf.impl.AMFParser;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.api.ApiReference;

import java.net.URL;
import java.nio.charset.Charset;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RecursiveShapePayloadValidatorTestCase {

  @Test
  public void RecursiveShapePayloadValidatorTest() throws Exception {
    String apiLocation = getResource("/org/mule/amf/impl/recursive-shape/testactivity.raml").toURI().toString();
    ApiReference apiRef = ApiReference.create(apiLocation);
    MimeType mimeType = new AMFParser(apiRef).parse().getResource("/visits").getAction("POST").getBody().get("application/json");
    final String validPayload =
        IOUtils.toString(getResource("/org/mule/amf/impl/recursive-shape/examples/post-visit-request.json").openStream(),
                         Charset.defaultCharset());
    final String invalidPayload =
        IOUtils.toString(getResource("/org/mule/amf/impl/recursive-shape/examples/post-visit-request-invalid.json").openStream(),
                         Charset.defaultCharset());
    assertTrue(mimeType.validate(validPayload).isEmpty());
    assertFalse(mimeType.validate(invalidPayload).isEmpty());
  }

  private static URL getResource(String s) {
    return RecursiveShapePayloadValidatorTestCase.class.getResource(s);
  }
}
