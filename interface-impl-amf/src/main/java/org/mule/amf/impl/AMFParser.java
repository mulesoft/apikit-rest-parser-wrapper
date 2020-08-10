/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import amf.client.AMF;
import amf.client.execution.ExecutionEnvironment;
import amf.client.model.document.BaseUnit;
import amf.client.model.document.Document;
import amf.client.model.domain.WebApi;
import amf.client.validate.ValidationReport;
import amf.plugins.xml.XmlValidationPlugin;
import org.mule.amf.impl.model.AMFImpl;
import org.mule.amf.impl.parser.factory.AMFParserWrapper;
import org.mule.amf.impl.parser.factory.AMFParserWrapperFactory;
import org.mule.amf.impl.parser.rule.ApiValidationResultImpl;
import org.mule.apikit.ApiParser;
import org.mule.apikit.common.LazyValue;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.validation.ApiValidationReport;
import org.mule.apikit.validation.ApiValidationResult;
import org.mule.apikit.validation.DefaultApiValidationReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.stream.Collectors.toList;

public class AMFParser implements ApiParser {

  private static final Logger logger = LoggerFactory.getLogger(AMFParser.class);

  private URI apiUri;
  private ApiReference apiRef;
  private AMFParserWrapper parser;
  private LazyValue<WebApi> webApi;
  private LazyValue<Document> document;
  private ExecutionEnvironment executionEnvironment;

  @Deprecated
  public AMFParser(ApiReference apiRef, boolean validate, ScheduledExecutorService scheduler) {
    initializeParser(apiRef, new ExecutionEnvironment(scheduler));
  }

  @Deprecated
  public AMFParser(ApiReference apiRef, boolean validate) {
    initializeParser(apiRef, new ExecutionEnvironment());
  }

  public AMFParser(ApiReference apiRef, ScheduledExecutorService scheduler) {
    initializeParser(apiRef, new ExecutionEnvironment(scheduler));
  }

  public AMFParser(ApiReference apiRef) {
    initializeParser(apiRef, new ExecutionEnvironment());
  }

  private void initializeParser(ApiReference apiRef, ExecutionEnvironment executionEnvironment) {
    this.executionEnvironment = executionEnvironment;
    this.apiUri = apiRef.getPathAsUri();
    this.document = new LazyValue<>(() -> parser.parseApi(apiUri));
    this.webApi = new LazyValue<>(() -> (WebApi) document.get().encodes());
    this.apiRef = apiRef;
    initAMF();
    this.parser = AMFParserWrapperFactory.getParser(apiRef, executionEnvironment);
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

  public WebApi getWebApi() {
    return webApi.get();
  }

  @Override
  public ApiValidationReport validate() {
    ValidationReport validationReport = parser.getParsingReport(document.get());
    List<ApiValidationResult> results = new ArrayList<>(0);
    if (!validationReport.conforms()) {
      results = validationReport.results().stream().map(ApiValidationResultImpl::new).collect(toList());
    }
    return new DefaultApiValidationReport(results);
  }

  @Override
  public ApiSpecification parse() {
    // We are forced to create a brand new environment so this object (and therefore the original document) is not referenced anymore
    AMFParserWrapper parserWrapper = AMFParserWrapperFactory.getParser(apiRef, executionEnvironment);
    return new AMFImpl(webApi.get(), getReferences(document.get().references()), parserWrapper, apiRef.getVendor(),
                       apiRef.getLocation(), apiUri);
  }

  private void initAMF() {
    try {
      if (executionEnvironment != null) {
        AMF.init(executionEnvironment).get();
      } else {
        AMF.init().get();
      }
      amf.core.AMF.registerPlugin(new XmlValidationPlugin());
    } catch (final Exception e) {
      logger.error("Error initializing AMF", e);
      throw new RuntimeException(e);
    }
  }
}
