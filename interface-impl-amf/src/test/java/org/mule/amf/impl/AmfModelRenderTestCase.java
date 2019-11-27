/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import static org.junit.Assert.assertEquals;

import org.mule.amf.impl.model.AMFImpl;
import org.mule.apikit.model.api.ApiReference;

import org.junit.Test;

public class AmfModelRenderTestCase {

  @Test
  public void amfGetLocationReturnsApiRefLocation() throws Exception {
    String folderLocation = AmfModelRenderTestCase.class.getResource("").toURI() + "amf-model-render/";
    String apiLocation = folderLocation + "api-to-render.raml";
    ApiReference apiRef = ApiReference.create(apiLocation);

    AMFImpl amfObj = (AMFImpl) new AMFParser(apiRef, true).parse();
    assertEquals(amfObj.getLocation(), apiRef.getLocation());
  }

  private String sanitize(String model) {
    return model.replace("\\r", "").replace("\\n", "")
        .replaceAll("\\r", "").replaceAll("\\n", "");
  }
}
