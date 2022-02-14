/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.net.URISyntaxException;
import org.junit.Test;
import org.mule.amf.impl.model.AMFImpl;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.api.ApiReference;

public class OasPayloadValidationTestCase {

  @Test
  public void fieldDefinedAsNullableInBody() throws URISyntaxException {

    String payload = "{\n"
        + "          \"city\": \"Gates Mills\",\n"
        + "          \"country\": \"ARG\",\n"
        + "          \"latitude\": null,\n"
        + "          \"longitude\": null,\n"
        + "          \"postalCode\": \"44040\",\n"
        + "          \"state\": \"OH\",\n"
        + "          \"street\": \"1665 Berkshire Rd\"\n"
        + "        }";
    String apiLocation = OasPayloadValidationTestCase.class.getResource("oas30-payload/api.yaml").toURI().toString();
    ApiReference apiRef = ApiReference.create(apiLocation);
    AMFImpl api = (AMFImpl) new AMFParser(apiRef).parse();

    MimeType body = api.getResources().get("/homebuyingreport").getAction("POST").getBody().get("application/json");

    assertThat(body.validate(payload).size(), equalTo(0));
  }
}
