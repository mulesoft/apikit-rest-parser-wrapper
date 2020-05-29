/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import amf.ProfileName;
import amf.ProfileNames;
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
import amf.client.resolve.Resolver;
import amf.core.remote.Vendor;
import org.apache.commons.io.IOUtils;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.api.ApiReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import static amf.ProfileNames.OAS;
import static amf.ProfileNames.OAS20;
import static amf.ProfileNames.OAS30;
import static amf.ProfileNames.RAML;
import static amf.ProfileNames.RAML08;
import static amf.ProfileNames.RAML10;
import static org.apache.commons.io.FilenameUtils.getExtension;

public class VendorUtils {

    public enum VendorEx {
        RAML,
        OAS20_JSON,
        OAS20_YAML,
        OAS30_JSON,
        OAS30_YAML
    }

    public static VendorEx getVendor(final URI api) {
        final String ext = getExtension(api.getPath());
        return "RAML".equalsIgnoreCase(ext) ? VendorEx.RAML : deduceVendorFromContent(api);
    }

    private static VendorEx deduceVendorFromContent(final URI api) {

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(api.toURL().openStream()));

            final String firstLine = getFirstLine(in).toUpperCase();

            if (firstLine.contains("#%RAML"))
                return VendorEx.RAML;

            final boolean isJson = firstLine.startsWith("{") || firstLine.startsWith("[");
            // Some times swagger version is in the first line too, e.g. yaml files
            if (firstLine.contains("SWAGGER")) {
                return isJson ? VendorEx.OAS20_JSON : VendorEx.OAS20_YAML;
            }
            if (firstLine.contains("OPENAPI")) {
                return isJson ? VendorEx.OAS30_JSON : VendorEx.OAS30_YAML;
            }

            int lines = 0;
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.toUpperCase().contains("OPENAPI"))
                    return isJson ? VendorEx.OAS30_JSON : VendorEx.OAS30_YAML;
                if (inputLine.toUpperCase().contains("SWAGGER"))
                    return isJson ? VendorEx.OAS20_JSON : VendorEx.OAS20_YAML;
                if (++lines == 10)
                    break;
            }
        } catch (final Exception ignore) {
        } finally {
            IOUtils.closeQuietly(in);
        }

        return VendorEx.RAML; // default value
    }

    private static String getFirstLine(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            if (line.trim().length() > 0)
                return line;
        }
        return "";
    }

    static Resolver getResolverByVendor(ApiVendor apiVendor) {
        switch (apiVendor) {
            case RAML_10:
                return new Raml10Resolver();
            case OAS_30:
                return new Oas30Resolver();
            case OAS:
            case OAS_20:
                return new Oas20Resolver();
            default:
                return new Raml08Resolver();
        }
    }

    static ApiVendor getMatchingApiVendor(Vendor vendor) {
        if (Vendor.RAML10().equals(vendor)) {
            return ApiVendor.RAML_10;
        } else if (Vendor.RAML08().equals(vendor)) {
            return ApiVendor.RAML_08;
        } else if (Vendor.RAML().equals(vendor)) {
            return ApiVendor.RAML;
        } else if (Vendor.OAS30().equals(vendor)) {
            return ApiVendor.OAS_30;
        } else if (Vendor.OAS20().equals(vendor)) {
            return ApiVendor.OAS_20;
        } else if (Vendor.OAS().equals(vendor)) {
            return ApiVendor.OAS;
        }
        return ApiVendor.RAML;
    }

    static ProfileName getProfileNameByVendor(ApiVendor apiVendor) {
        switch (apiVendor) {
            case RAML_10:
                return RAML10();
            case OAS:
                return OAS();
            case OAS_20:
                return OAS20();
            case OAS_30:
                return OAS30();
            case RAML:
                return RAML();
            case RAML_08:
                return RAML08();
            default:
                return ProfileNames.AMF();
        }
    }

    static Parser getParserByVendor(ApiReference apiRef, Environment environment) {
        final ApiVendor vendor = apiRef.getVendor();
        switch (vendor) {
            case RAML_10:
                return new Raml10Parser(environment);
            case OAS:
            case OAS_20:
                if ("JSON".equalsIgnoreCase(apiRef.getFormat()))
                    return new Oas20Parser(environment);
                else
                    return new Oas20YamlParser(environment);
            case OAS_30:
                if ("JSON".equalsIgnoreCase(apiRef.getFormat()))
                    return new Oas30Parser(environment);
                else
                    return new Oas30YamlParser(environment);
            case RAML_08:
                return new Raml08Parser(environment);
            default:
                return new RamlParser(environment);
        }
    }
}
