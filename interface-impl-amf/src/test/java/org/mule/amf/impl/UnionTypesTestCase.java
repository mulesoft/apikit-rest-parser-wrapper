/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.validation.ApiValidationResult;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

// TODO restore after APIMF-2103 fix
@Ignore
@RunWith(Parameterized.class)
public class UnionTypesTestCase {

    private static final String BODY_UNION_RESOURCE = "/bodyunion";
    private static final String QUERY_PARAM_UNION_RESOURCE = "/queryparamunion";
    private static final String URI_PARAM_UNION_RESOURCE = "/uriparamunion/{dateortimestamp}";
    private static final String GET_ACTION = "GET";
    private static final String POST_ACTION = "POST";
    private static final String APPLICATION_JSON = "application/json";
    private static final String PARAM_VALUE = "dateortimestamp";

    @Parameterized.Parameter
    public ApiVendor apiVendor;

    @Parameterized.Parameter(1)
    public ApiSpecification api;

    @Parameterized.Parameters(name = "{0}")
    public static Collection apiSpecifications() throws Exception {
        String apiLocation = UnionTypesTestCase.class.getResource("union-types/union-types-api.raml").toURI().toString();
        ApiReference ramlApiRef = ApiReference.create(apiLocation);

        apiLocation = UnionTypesTestCase.class.getResource("union-types/union-types-api.yaml").toURI().toString();
        ApiReference oas30apiRef = ApiReference.create(apiLocation);

        return Arrays.asList(new Object[][]{
                {ApiVendor.RAML, new AMFParser(ramlApiRef, true).parse()},
                {ApiVendor.OAS_30, new AMFParser(oas30apiRef, true).parse()}
        });
    }

    @Test
    public void testUnionValidations() {
        List<ApiValidationResult> report = api.getResource(BODY_UNION_RESOURCE).getAction(POST_ACTION).getBody().get(APPLICATION_JSON).validate("Hello World");
        assertNotNull(report);
        assertEquals(2, report.size());
        assertFalse(api.getResource(QUERY_PARAM_UNION_RESOURCE).getAction(GET_ACTION).getQueryParameters().get(PARAM_VALUE).validate("Hello%20World"));
        assertFalse(api.getResource(URI_PARAM_UNION_RESOURCE).getAction(GET_ACTION).getResolvedUriParameters().get(PARAM_VALUE).validate("HelloWorld"));
    }

}
