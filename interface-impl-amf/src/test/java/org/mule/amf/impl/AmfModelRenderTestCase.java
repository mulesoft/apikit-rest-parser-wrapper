/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import static java.nio.file.Files.readAllBytes;
import static org.junit.Assert.assertEquals;

import org.mule.amf.impl.model.AMFImpl;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.api.ApiReference;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.junit.Test;

public class AmfModelRenderTestCase {

  @Test
  public void renderTestCase() throws Exception {
    String folderLocation = AmfModelRenderTestCase.class.getResource("").toURI() + "amf-model-render/";
    String apiLocation = folderLocation + "api-to-render.raml";
    byte[] expected = readAllBytes(Paths.get(new URI(folderLocation + "golden-amf-model.json")));
    String goldenAmfModel = new String(expected, StandardCharsets.UTF_8);

    ApiReference apiRef = ApiReference.create(apiLocation);
    final AMFImpl amfParser = (AMFImpl) new AMFParser(apiRef, true).parse();
    assertEquals(sanitize(goldenAmfModel), sanitize(amfParser.dumpAmf()));
  }

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
