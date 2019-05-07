/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import amf.ProfileName;
import amf.client.AMF;
import amf.client.environment.DefaultEnvironment;
import amf.client.environment.Environment;
import amf.client.model.document.BaseUnit;
import amf.client.model.document.Document;
import amf.client.model.domain.WebApi;
import amf.client.parse.Parser;
import amf.client.resolve.Raml10Resolver;

import org.mule.amf.impl.loader.ExchangeDependencyResourceLoader;
import org.mule.amf.impl.loader.ProvidedResourceLoader;
import org.mule.amf.impl.model.AMFImpl;
import org.mule.amf.impl.parser.rule.ApiValidationResultImpl;
import org.mule.apikit.ParserType;
import org.mule.apikit.ApiParser;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.api.ApiRef;
import org.mule.apikit.validation.DefaultApiValidationReport;
import org.mule.apikit.validation.ApiValidationReport;
import org.mule.apikit.validation.ApiValidationResult;

import amf.client.validate.ValidationReport;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static amf.ProfileNames.AMF;
import static amf.ProfileNames.OAS;
import static amf.ProfileNames.OAS20;
import static amf.ProfileNames.RAML;
import static amf.ProfileNames.RAML08;
import static amf.ProfileNames.RAML10;
import static java.util.stream.Collectors.toList;
import static org.mule.amf.impl.AMFUtils.getPathAsUri;
import static org.mule.amf.impl.DocumentParser.getParserForApi;

public class AMFParser implements ApiParser {

  private final ApiRef apiRef;
  private final Parser parser;
  private final WebApi webApi;
  private final ApiVendor apiVendor;
  private final List<String> references;

  private Document consoleModel;

  private AMFParser(ApiRef apiRef, boolean validate) throws ExecutionException, InterruptedException {
    AMF.init().get();
    this.apiRef = apiRef;
    this.parser = initParser(apiRef);

    Document document = buildDocument(validate);
    this.references = getReferences(document.references());
    this.webApi = DocumentParser.getWebApi(document);
    this.apiVendor = apiRef.getVendor();
  }

  public static AMFParser create(ApiRef apiRef, boolean validate) throws Exception {
    return new AMFParser(apiRef, validate);
  }

  private Parser initParser(ApiRef apiRef) {
    final Environment environment = buildEnvironment(apiRef);
    return getParserForApi(apiRef, environment);
  }

  private List<String> getReferences(List<BaseUnit> references) {
    List<String> result = new ArrayList<>();
    appendReferences(references, new HashSet<>(), result);
    return result;
  }

  private void appendReferences(final List<BaseUnit> references, final Set<String> alreadyAdded, final List<String> result) {
    for (final BaseUnit reference : references) {
      final String id = reference.id();
      if (!alreadyAdded.contains(id)) {
        final String location = reference.location();
        result.add(location);
        alreadyAdded.add(id);
        appendReferences(reference.references(), alreadyAdded, result);
      }
    }
  }

  private Environment buildEnvironment(ApiRef apiRef) {
    final URI uri = getPathAsUri(apiRef);

    Environment environment = DefaultEnvironment.apply();

    if (uri.getScheme() != null && uri.getScheme().startsWith("file")) {
      final File file = new File(uri);
      final String rootDir = file.isDirectory() ? file.getPath() : file.getParent();
      environment = environment.add(new ExchangeDependencyResourceLoader(rootDir));
    }

    if (apiRef.getResourceLoader().isPresent()) {
      environment = environment.add(new ProvidedResourceLoader(apiRef.getResourceLoader().get()));
    }

    return environment;
  }

  @Override
  public ApiVendor getApiVendor() {
    return apiVendor;
  }

  @Override
  public ParserType getParserType() {
    return ParserType.AMF;
  }

  public WebApi getWebApi() {
    return webApi;
  }

  @Override
  public ApiValidationReport validate() {
    ValidationReport validationReport = generateValidationReport();
    List<ApiValidationResult> results = new ArrayList<>(0);
    if (!validationReport.conforms()) {
      results = validationReport.results().stream().map(ApiValidationResultImpl::new).collect(toList());
    }
    return new DefaultApiValidationReport(results);
  }

  private ValidationReport generateValidationReport() {
    final ValidationReport validationReport;
    try {
      validationReport = parser.reportValidation(apiVendorToProfileName(apiVendor)).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Unexpected error parsing API: " + e.getMessage(), e);
    }
    return validationReport;
  }

  private ProfileName apiVendorToProfileName(ApiVendor apiVendor) {
    switch (apiVendor) {
      case OAS:
        return OAS();
      case OAS_20:
        return OAS20();
      case RAML:
        return RAML();
      case RAML_08:
        return RAML08();
      case RAML_10:
        return RAML10();
      default:
        return AMF();
    }
  }

  @Override
  public ApiSpecification parse() {
    return new AMFImpl(webApi, references, apiVendor, getConsoleModel(), apiRef);
  }

  private Document getConsoleModel() {
    if (consoleModel == null) {
      Document document = buildDocument(false);
      consoleModel = (Document) new Raml10Resolver().resolve(document, "editing");
    }
    return consoleModel;
  }

  private Document buildDocument(boolean validate) {
    final URI uri = getPathAsUri(apiRef);
    return DocumentParser.parseFile(parser, uri, validate);
  }
}
