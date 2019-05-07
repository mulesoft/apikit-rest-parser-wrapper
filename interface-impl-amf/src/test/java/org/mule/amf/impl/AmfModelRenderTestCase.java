/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import org.junit.Test;

import org.mule.amf.impl.model.AMFImpl;
import org.mule.apikit.implv1.model.RamlImplV1;
import org.mule.apikit.model.api.ApiRef;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static java.nio.file.Files.readAllBytes;
import static org.junit.Assert.assertEquals;

public class AmfModelRenderTestCase {

  @Test
  public void renderTestCase() throws Exception {
    String folderLocation = AmfModelRenderTestCase.class.getResource("").toURI() + "amf-model-render/";
    String apiLocation = folderLocation + "api-to-render.raml";
    byte[] expected = readAllBytes(Paths.get(new URI(folderLocation + "golden-amf-model.json")));
    String goldenAmfModel = new String(expected, StandardCharsets.UTF_8);

    ApiRef apiRef = ApiRef.create(apiLocation);
    String amfModel = ((AMFImpl) AMFParser.create(apiRef, true).parse()).dumpAmf();
    assertEquals(goldenAmfModel, amfModel);
  }
}
