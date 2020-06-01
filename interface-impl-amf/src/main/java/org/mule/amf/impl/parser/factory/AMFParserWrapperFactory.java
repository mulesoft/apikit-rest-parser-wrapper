/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.parser.factory;

import amf.client.environment.Environment;
import amf.client.parse.Oas20Parser;
import amf.client.parse.Oas20YamlParser;
import amf.client.parse.Oas30Parser;
import amf.client.parse.Oas30YamlParser;
import amf.client.parse.Parser;
import amf.client.parse.Raml08Parser;
import amf.client.parse.Raml10Parser;
import amf.client.parse.RamlParser;
import amf.client.resolve.Oas20Resolver;
import amf.client.resolve.Oas30Resolver;
import amf.client.resolve.Raml08Resolver;
import amf.client.resolve.Raml10Resolver;
import org.mule.apikit.model.ApiFormat;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.api.ApiReference;

import static amf.ProfileNames.AMF;
import static amf.ProfileNames.OAS20;
import static amf.ProfileNames.OAS30;
import static amf.ProfileNames.RAML08;
import static amf.ProfileNames.RAML10;

public class AMFParserWrapperFactory {

    private AMFParserWrapperFactory() {
    }

    public static AMFParserWrapper getParser(ApiReference apiRef, Environment environment) {
        if (environment == null) {
            throw new RuntimeException("Environment is mandatory, please provide one.");
        }
        final ApiVendor apiVendor = apiRef.getVendor();
        final String apiFormat = apiRef.getFormat();
        switch (apiVendor) {
            case RAML_10:
                return new AMFParserWrapper(new Raml10Parser(environment), new Raml10Resolver(), RAML10());
            case OAS:
            case OAS_20:
                Parser oas20Parser = ApiFormat.JSON.name().equalsIgnoreCase(apiFormat) ? new Oas20Parser(environment) : new Oas20YamlParser(environment);
                return new AMFParserWrapper(oas20Parser, new Oas20Resolver(), OAS20());
            case OAS_30:
                Parser oas30Parser = ApiFormat.JSON.name().equalsIgnoreCase(apiFormat) ? new Oas30Parser(environment) : new Oas30YamlParser(environment);
                return new AMFParserWrapper(oas30Parser, new Oas30Resolver(), OAS30());
            case RAML_08:
                return new AMFParserWrapper(new Raml08Parser(environment), new Raml08Resolver(), RAML08());
            default:
                return new AMFParserWrapper(new RamlParser(environment), new Raml08Resolver(), AMF());
        }
    }
}
