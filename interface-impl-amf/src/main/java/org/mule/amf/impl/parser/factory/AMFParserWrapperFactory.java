/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.parser.factory;

import amf.MessageStyles;
import amf.client.environment.DefaultEnvironment;
import amf.client.environment.Environment;
import amf.client.execution.ExecutionEnvironment;
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
import org.mule.amf.impl.loader.ExchangeDependencyResourceLoader;
import org.mule.amf.impl.loader.ProvidedResourceLoader;
import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.ApiFormat;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.api.ApiReference;

import java.io.File;
import java.net.URI;
import java.util.Optional;

import static amf.ProfileNames.AMF;
import static amf.ProfileNames.OAS20;
import static amf.ProfileNames.OAS30;
import static amf.ProfileNames.RAML08;
import static amf.ProfileNames.RAML10;

public class AMFParserWrapperFactory {

    private AMFParserWrapperFactory() {
    }

    public static AMFParserWrapper getParser(ApiReference apiRef, ExecutionEnvironment execEnv) {
        if (execEnv == null) {
            throw new RuntimeException("ExecutionEnvironment is mandatory");
        }
        final ApiVendor apiVendor = apiRef.getVendor();
        final String apiFormat = apiRef.getFormat();
        Environment environment = buildEnvironment(apiRef, execEnv);
        switch (apiVendor) {
            case RAML_10:
                return new AMFParserWrapper(environment, new Raml10Parser(environment), new Raml10Resolver(), RAML10(), MessageStyles.RAML());
            case OAS:
            case OAS_20:
                Parser oas20Parser = ApiFormat.JSON.name().equalsIgnoreCase(apiFormat) ? new Oas20Parser(environment) : new Oas20YamlParser(environment);
                return new AMFParserWrapper(environment, oas20Parser, new Oas20Resolver(), OAS20(), MessageStyles.OAS());
            case OAS_30:
                Parser oas30Parser = ApiFormat.JSON.name().equalsIgnoreCase(apiFormat) ? new Oas30Parser(environment) : new Oas30YamlParser(environment);
                return new AMFParserWrapper(environment, oas30Parser, new Oas30Resolver(), OAS30(), MessageStyles.OAS());
            case RAML_08:
                return new AMFParserWrapper(environment, new Raml08Parser(environment), new Raml08Resolver(), RAML08(), MessageStyles.RAML());
            default:
                return new AMFParserWrapper(environment, new RamlParser(environment), new Raml08Resolver(), AMF(), MessageStyles.AMF());
        }
    }

    private static Environment buildEnvironment(ApiReference apiRef, ExecutionEnvironment executionEnvironment) {
        Environment environment = DefaultEnvironment.apply(executionEnvironment);
        URI apiUri = apiRef.getPathAsUri();
        if (apiUri.getScheme() != null && apiUri.getScheme().startsWith("file")) {
            final File file = new File(apiUri);
            final String rootDir = file.isDirectory() ? file.getPath() : file.getParent();
            environment = environment.add(new ExchangeDependencyResourceLoader(rootDir, executionEnvironment));
        }

        Optional<ResourceLoader> resourceLoader = apiRef.getResourceLoader();
        if (resourceLoader.isPresent()) {
            environment = environment.add(new ProvidedResourceLoader(resourceLoader.get()));
        }

        return environment;
    }

}
