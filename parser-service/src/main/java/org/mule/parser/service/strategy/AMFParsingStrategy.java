/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.strategy;

import org.mule.amf.impl.AMFParser;
import org.mule.amf.impl.model.AMFImpl;
import org.mule.apikit.ApiParser;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.validation.ApiValidationReport;
import org.mule.parser.service.result.DefaultParseResult;
import org.mule.parser.service.result.DefaultParsingIssue;
import org.mule.parser.service.result.ExceptionParseResult;
import org.mule.parser.service.result.ParseResult;
import org.mule.parser.service.result.ParsingIssue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.Collections.emptyList;
import static org.mule.parser.service.strategy.ValidationReportHelper.errors;
import static org.mule.parser.service.strategy.ValidationReportHelper.warnings;

public class AMFParsingStrategy implements ParsingStrategy {

  private final boolean validate;
  private ScheduledExecutorService executor;

  public AMFParsingStrategy() {
    this(true);
  }

  public AMFParsingStrategy(boolean validate) {
    this.validate = validate;
  }

  @Override
  public ParseResult parse(ApiReference ref) {
    try {
      ApiParser parser = create(ref);
      if (!validate) {
        return new DefaultParseResult(parser.parse(), emptyList(), emptyList());
      }
      ApiValidationReport report = parser.validate();
      AMFImpl apiSpec = (AMFImpl) parser.parse();
      List<ParsingIssue> warnings = warnings(report);
      warnings.addAll(getUnsupportedFeaturesWarnings(apiSpec));
      return new DefaultParseResult(apiSpec, errors(report), warnings);
    } catch (Exception e) {
      return new ExceptionParseResult(e);
    }
  }

  private List<ParsingIssue> getUnsupportedFeaturesWarnings(AMFImpl apiSpecification) {
    List<ParsingIssue> unsupportedFeatureMessages = new ArrayList<>();
    if (apiSpecification.includesCallbacks()) {
      unsupportedFeatureMessages.add(new DefaultParsingIssue("OAS 3 - Callbacks are not supported yet."));
    }
    if (apiSpecification.includesLinks()) {
      unsupportedFeatureMessages.add(new DefaultParsingIssue("OAS 3 - Links are not supported yet"));
    }
    return unsupportedFeatureMessages;
  }

  @Override
  public void setExecutor(ScheduledExecutorService executor) {
    this.executor = executor;
  }

  private AMFParser create(ApiReference ref) {
    return new AMFParser(ref, executor);
  }
}
