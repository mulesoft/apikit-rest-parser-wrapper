/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import amf.ProfileName;
import amf.ProfileNames;
import amf.client.AMF;
import amf.client.environment.Environment;
import amf.client.execution.ExecutionEnvironment;
import amf.client.model.document.BaseUnit;
import amf.client.model.document.Document;
import amf.client.model.domain.WebApi;
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
import amf.client.validate.ValidationReport;
import amf.client.validate.ValidationResult;
import amf.core.remote.Vendor;
import amf.plugins.document.webapi.resolution.pipelines.AmfResolutionPipeline;
import amf.plugins.xml.XmlValidationPlugin;
import org.apache.commons.io.IOUtils;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.api.ApiReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static amf.ProfileNames.OAS;
import static amf.ProfileNames.OAS20;
import static amf.ProfileNames.OAS30;
import static amf.ProfileNames.RAML;
import static amf.ProfileNames.RAML08;
import static amf.ProfileNames.RAML10;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.mule.amf.impl.AMFUtils.getPathAsUri;


public class DocumentParser {

  private static final Logger logger = LoggerFactory.getLogger(DocumentParser.class);

  private DocumentParser() {
  }

  private static <T, U> U handleFuture(CompletableFuture<T> f) throws ParserException {
    try {
      return (U) f.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new ParserException("An error happened while parsing the api. Message: " + e.getMessage(), e);
    }
  }

  public static Document parseFile(final Parser parser, final ApiReference apiRef, final boolean validate) throws ParserException {
    final URI uri = getPathAsUri(apiRef);
    final ApiVendor apiVendor = apiRef.getVendor();
    return parseFile(parser, uri, apiVendor, validate);
  }

  private static Document parseFile(final Parser parser, final URI uri, final ApiVendor apiVendor, final boolean validate)
          throws ParserException {
    Document document = parseFile(parser, uriToPath(uri));
    Resolver resolver = getResolverByVendor(apiVendor);
    document = (Document) resolver.resolve(document, AmfResolutionPipeline.EDITING_PIPELINE());

    if (validate) {
      final ValidationReport parsingReport = DocumentParser.getParsingReport(parser, apiVendor);
      if (!parsingReport.conforms()) {
        final List<ValidationResult> results = parsingReport.results();
        if (!results.isEmpty()) {
          final String message = results.get(0).message();
          throw new ParserException(message);
        }
      }
    }
    return document;
  }

  private static String uriToPath(final URI uri) {
    final String path = uri.toString();
    return URLDecoder.decode(path);
  }

  private static Document parseFile(final Parser parser, final String url) throws ParserException {
    return handleFuture(parser.parseFileAsync(url));
  }

  public static Parser getParserForApi(final ApiReference apiRef, Environment environment, ExecutionEnvironment executionEnvironment) {
    init(executionEnvironment);
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

  public static WebApi getWebApi(final BaseUnit baseUnit) throws ParserException {
    Resolver resolver = getResolverByVendor(baseUnit.sourceVendor().orElse(Vendor.RAML10()));
    Document document = (Document) resolver.resolve(baseUnit, AmfResolutionPipeline.EDITING_PIPELINE());
    return (WebApi) document.encodes();
  }

  public static ValidationReport getParsingReport(final Parser parser, final ApiVendor apiVendor) throws ParserException {
    final ProfileName profile = apiVendorToProfileName(apiVendor);
    return handleFuture(parser.reportValidation(profile));
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
  
  private static void init(ExecutionEnvironment executionEnvironment){
    try {
      if (executionEnvironment != null) {
        AMF.init(executionEnvironment).get();
      } else {
        AMF.init().get();
      }
//      AMFValidatorPlugin.withEnabledValidation(true);
      amf.core.AMF.registerPlugin(new XmlValidationPlugin());
    } catch (final Exception e) {
      logger.error("Error initializing AMF", e);
      throw new RuntimeException(e);
    }
  }

  private static Resolver getResolverByVendor(ApiVendor apiVendor) {
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

  private static Resolver getResolverByVendor(Vendor vendor) {
    if (Vendor.RAML10().equals(vendor)) {
      return new Raml10Resolver();
    } else if (Vendor.OAS().equals(vendor) || Vendor.OAS20().equals(vendor)) {
      return new Oas20Resolver();
    } else if (Vendor.OAS30().equals(vendor)) {
      return new Oas30Resolver();
    } else {
      return new Raml08Resolver();
    }
  }

  public static ProfileName apiVendorToProfileName(ApiVendor apiVendor) {
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

  public enum VendorEx {
    RAML,
    OAS20_JSON,
    OAS20_YAML,
    OAS30_JSON,
    OAS30_YAML
  }
}
