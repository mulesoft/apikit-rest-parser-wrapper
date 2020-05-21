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
  public static final String AMF_TITLE = "\nAMF: ";
  public static final String RAML_TITLE = "\nRAML: ";
  private ScheduledExecutorService executor;

  @Override
  public ParseResult parse(ApiReference ref) {
    ParseResult parseResult = AMF_DELEGATE.parse(ref);
    if (!parseResult.success() && StringUtils.equals(ref.getFormat(), RAML_FORMAT)){
      ReferencesResolver referencesResolver = createReferencesResolver(parseResult);
      ParseResult ramlResult = new RamlParsingStrategy(referencesResolver).parse(ref);
      List<ParsingIssue> errors = joinParsingIssues(parseResult.getErrors(), ramlResult.getErrors());
      List<ParsingIssue> warnings = joinParsingIssues(parseResult.getWarnings(), ramlResult.getWarnings());
      DefaultParseResult delegate = createDelegate(ramlResult, errors, warnings);
      parseResult = new FallbackParseResult(delegate);
    }
    return parseResult;
  }
/** Used for cases when AMF parser fail and RAML is ok. The result in the test "fallbackParsingAMFErrorRAMLOk" was a failure because it had
 * success false and errors.size() = 1, because the errors from AMF remained due to the join.
 * This is why I had to sanitize these lists and put them as new ArrayList<>().
 * @param sourceErrors previous errors incoming from AMF
 * @param sourceWarnings previous warnings incoming from AMF
 * */
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

  private ReferencesResolver createReferencesResolver(ParseResult amfResult) {
      ReferencesResolver referencesResolver = new ReferencesResolver(amfResult);
      if(executor != null){
        referencesResolver.setExecutor(executor);
      }
    return referencesResolver;
  }

  private List<ParsingIssue> joinParsingIssues(List<ParsingIssue> amfIssues, List<ParsingIssue> ramlIssues) {
    Stream<DefaultParsingIssue> amfIssuesWithTitle = amfIssues.stream().map(issue -> new DefaultParsingIssue(AMF_TITLE + issue.cause()));
    Stream<DefaultParsingIssue> ramlIssuesWithTitle = ramlIssues.stream().map(issue -> new DefaultParsingIssue(RAML_TITLE + issue.cause()));
    return Stream.concat(amfIssuesWithTitle, ramlIssuesWithTitle).collect(Collectors.toList());
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
