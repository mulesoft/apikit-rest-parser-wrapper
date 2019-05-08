/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.strategy;

import static org.mule.parser.service.strategy.ValidationReportHelper.errors;
import static org.mule.parser.service.strategy.ValidationReportHelper.warnings;

import org.mule.amf.impl.AMFParser;
import org.mule.apikit.ApiParser;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.validation.ApiValidationReport;
import org.mule.parser.service.result.DefaultParseResult;
import org.mule.parser.service.result.ExceptionParseResult;
import org.mule.parser.service.result.ParseResult;

import java.util.concurrent.ExecutionException;

public class AMFParsingStrategy implements ParsingStrategy {

  @Override
  public ParseResult parse(ApiReference ref) {
    try {
      ApiParser parser = create(ref);
      ApiValidationReport report = parser.validate();
      return new DefaultParseResult(parser.parse(), errors(report), warnings(report));
    } catch (Exception e) {
      return new ExceptionParseResult(e);
    }
  }

  private AMFParser create(ApiReference ref) {
    try {
      return new AMFParser(ref, false);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
