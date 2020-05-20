/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.strategy;


import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.lang.StringUtils;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.references.ReferencesResolver;
import org.mule.parser.service.result.DefaultParseResult;
import org.mule.parser.service.result.DefaultParsingIssue;
import org.mule.parser.service.result.ParseResult;
import org.mule.parser.service.result.ParsingIssue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

public class WithFallbackParsingStrategy implements ParsingStrategy {
  private static final AMFParsingStrategy AMF_DELEGATE = new AMFParsingStrategy();
  public static final String RAML_FORMAT = "RAML";
  private ScheduledExecutorService executor;

  @Override
  public ParseResult parse(ApiReference ref) {
    ParseResult parseResult = AMF_DELEGATE.parse(ref);
    if (!parseResult.success() && StringUtils.equals(ref.getFormat(), RAML_FORMAT)){
      ParseResult ramlResult = new RamlParsingStrategy(addExecutor(parseResult)).parse(ref);
      List<ParsingIssue> errors = joinErrors(parseResult.getErrors(), ramlResult.getErrors());
      List<ParsingIssue> warnings = joinErrors(parseResult.getWarnings(), ramlResult.getWarnings());
      DefaultParseResult delegate = createDelegate(ramlResult, errors, warnings);
      parseResult = new FallbackParseResult(delegate);
    }
    return parseResult;
  }

  private DefaultParseResult createDelegate(ParseResult ramlResult, List<ParsingIssue> sourceErrors, List<ParsingIssue> sourceWarnings) {
    List<ParsingIssue> errors = new ArrayList<>();
    List<ParsingIssue> warnings = new ArrayList<>();
    if(!ramlResult.success()){
      errors = sourceErrors;
      warnings = sourceWarnings;
    }
    DefaultParseResult defaultParseResult = new DefaultParseResult(ramlResult.get(), errors, warnings);
    return defaultParseResult;
  }

  private ReferencesResolver addExecutor(ParseResult amfResult) {
      ReferencesResolver referencesResolver = new ReferencesResolver(amfResult);
      if(executor != null){
        referencesResolver.setExecutor(executor);
      }
    return referencesResolver;
  }

  private List<ParsingIssue> joinErrors(List<ParsingIssue> amfIssues, List<ParsingIssue> ramlIssues) {
    List<ParsingIssue> amfIssuesWithTitle = new ArrayList<>();
    List<ParsingIssue> ramlIssuesWithTitle = new ArrayList<>();
    amfIssues.forEach(parsingIssue ->{amfIssuesWithTitle.add(new DefaultParsingIssue("\nAMF: " + parsingIssue.cause()));});
    ramlIssues.forEach(parsingIssue ->{ramlIssuesWithTitle.add(new DefaultParsingIssue("\nRAML: " + parsingIssue.cause()));});
    return Stream.concat(amfIssuesWithTitle.stream(), ramlIssuesWithTitle.stream()).collect(Collectors.toList());
  }

  @Override
  public void setExecutor(ScheduledExecutorService executor) {
    this.executor = executor;
    AMF_DELEGATE.setExecutor(executor);
  }

  public class FallbackParseResult implements ParseResult {

    private final ParseResult delegate;

    FallbackParseResult(ParseResult delegate) {
      this.delegate = delegate;
    }

    @Override
    public ApiSpecification get() {
      return delegate.get();
    }

    @Override
    public boolean success() {
      return delegate.success();
    }

    @Override
    public List<ParsingIssue> getErrors() {
      return delegate.getErrors();
    }

    @Override
    public List<ParsingIssue> getWarnings() {
      return ImmutableList.<ParsingIssue>builder()
        .add(new DefaultParsingIssue("AMF parsing failed, fallback into RAML parser"))
        .addAll(delegate.getWarnings()).build();
    }
  }
}
