/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import amf.apicontract.client.platform.model.domain.api.WebApi;
import amf.core.client.platform.execution.ExecutionEnvironment;
import amf.core.client.platform.model.document.BaseUnit;
import amf.core.client.platform.model.document.Document;
import amf.core.client.platform.validation.AMFValidationReport;
import org.mule.amf.impl.model.AMFImpl;
import org.mule.amf.impl.parser.factory.AMFParserWrapper;
import org.mule.amf.impl.parser.rule.ApiValidationResultImpl;
import org.mule.amf.impl.util.LazyValue;
import org.mule.apikit.ApiParser;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.validation.ApiValidationReport;
import org.mule.apikit.validation.ApiValidationResult;
import org.mule.apikit.validation.DefaultApiValidationReport;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.stream.Collectors.toList;

public class AMFParser implements ApiParser {


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
    this.document = new LazyValue<>(() -> parser.parseApi());
    this.webApi = new LazyValue<>(() -> (WebApi) document.get().encodes());
    this.apiRef = apiRef;
    this.parser = getParser(apiRef, executionEnvironment);
  }

  public static AMFParserWrapper getParser(ApiReference apiRef, ExecutionEnvironment execEnv) {
    if (execEnv == null) {
      throw new RuntimeException("ExecutionEnvironment is mandatory");
    }

    return new AMFParserWrapper(apiRef, execEnv);
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
    AMFValidationReport validationReport = parser.getParsingReport(document.get());
    List<ApiValidationResult> results = new ArrayList<>(0);
    if (!validationReport.conforms()) {
      results = validationReport.results().stream().map(ApiValidationResultImpl::new).collect(toList());
    }
    results.addAll(parser.getParsingIssues().stream().map(ApiValidationResultImpl::new).collect(toList()));
    return new DefaultApiValidationReport(results);
  }

  @Override
  public ApiSpecification parse() {
    // We are forced to create a brand new environment so this object (and therefore the original document) is not referenced
    // anymore
    AMFParserWrapper parserWrapper = getParser(apiRef, executionEnvironment);
    return new AMFImpl(webApi.get(), getReferences(document.get().references()), parserWrapper, apiRef.getVendor(),
                       apiRef.getLocation(), apiUri);
  }

}
