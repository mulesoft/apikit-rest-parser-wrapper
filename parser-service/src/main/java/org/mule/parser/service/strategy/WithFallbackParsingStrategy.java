/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.strategy;

import java.util.concurrent.ScheduledExecutorService;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.references.ReferencesResolver;
import org.mule.parser.service.result.DefaultParsingIssue;
import org.mule.parser.service.result.ParseResult;
import org.mule.parser.service.result.ParsingIssue;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class WithFallbackParsingStrategy implements ParsingStrategy {
  private static final AMFParsingStrategy AMF_DELEGATE = new AMFParsingStrategy();
  private ScheduledExecutorService executor;

  @Override
  public ParseResult parse(ApiReference ref) {
    if(executor != null){
      AMF_DELEGATE.setExecutor(executor);
    }
    ParseResult amfResult = AMF_DELEGATE.parse(ref);
    if (amfResult.success()) {
      return amfResult;
    }
    ParseResult ramlResult = new RamlParsingStrategy(new ReferencesResolver(amfResult)).parse(ref);
    return new FallbackParseResult(ramlResult);
  }

  @Override
  public void setExecutor(ScheduledExecutorService executor) {
    this.executor = executor;
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
