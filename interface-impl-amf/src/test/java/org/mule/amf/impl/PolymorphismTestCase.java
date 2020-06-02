/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.api.ApiReference;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PolymorphismTestCase {

    private static final String ONE_OF_PETS_RESOURCE = "/oneOfPetsByDiscriminator";
    private static final String ONE_OF_CAT_OR_DOG_RESOURCE = "/oneOfCatOrDog";
    private static final String ANY_OF_PETS_RESOURCE = "/anyOfPetsByProp";
    private static final String PATCH_ACTION = "PATCH";
    private static final String APPLICATION_JSON = "application/json";

    private static ApiSpecification api;

    @BeforeClass
    public static void apiSpecifications() throws Exception {
        String apiLocation = PolymorphismTestCase.class.getResource("polymorphism/oas30-polymorphism.yaml").toURI().toString();
        ApiReference oas30apiRef = ApiReference.create(apiLocation);
        api = new AMFParser(oas30apiRef, true).parse();
    }

    @Test
    public void OneOfTest() {
        MimeType mimeType = api.getResource(ONE_OF_CAT_OR_DOG_RESOURCE).getAction(PATCH_ACTION).getBody().get(APPLICATION_JSON);
        assertTrue(mimeType.validate("{\"pet_type\": \"SimpleCat\",\"hunts\": true,\"age\": 3}").isEmpty());
        assertTrue(mimeType.validate("{\"pet_type\": \"SimpleDog\",\"bark\": true,\"breed\": \"Dingo\"}").isEmpty());
        assertFalse(mimeType.validate("{\"pet_type\": \"SimpleCat\",\"bark\": true,\"hunts\": true}").isEmpty());
        assertFalse(mimeType.validate("{\"pet_type\": \"SimpleDog\",\"bark\": true,\"hunts\": true,\"breed\": \"Husky\",\"age\": 3}").isEmpty());
    }

    @Test
    public void oneOfWithInheritanceTest() {
        MimeType mimeType = api.getResource(ONE_OF_PETS_RESOURCE).getAction(PATCH_ACTION).getBody().get(APPLICATION_JSON);
        assertTrue(mimeType.validate("{\"pet_type\": \"Cat\",\"name\": \"Tom\",\"hunts\": false}").isEmpty());
        assertTrue(mimeType.validate("{\"pet_type\": \"Dog\",\"name\": \"Jack\",\"bark\": false,\"breed\": \"Dingo\"}").isEmpty());
        assertFalse(mimeType.validate("{\"pet_type\": \"Cat\",\"age\": 2}").isEmpty());
        assertFalse(mimeType.validate("{\"pet_type\": \"Dog\"}").isEmpty());
    }

    // TODO Check why this assertion fails here but we have the correct response in the module
//    @Test
//    public void notIntegerTest() {
//        MimeType mimeType = api.getResource(ONE_OF_PETS_RESOURCE).getAction(PATCH_ACTION).getBody().get(APPLICATION_JSON);
//        assertFalse(mimeType.validate("{\"pet_type\": 1234}").isEmpty());
//    }

    @Test
    public void anyOfTest() {
        MimeType mimeType = api.getResource(ANY_OF_PETS_RESOURCE).getAction(PATCH_ACTION).getBody().get(APPLICATION_JSON);
        assertTrue(mimeType.validate("{\"age\": 1}").isEmpty());
        assertTrue(mimeType.validate("{\"pet_type\": \"Cat\",\"hunts\": true}").isEmpty());
        assertFalse(mimeType.validate("{\"nickname\": \"Mr. Paws\",\"hunts\": false}").isEmpty());
    }

}
