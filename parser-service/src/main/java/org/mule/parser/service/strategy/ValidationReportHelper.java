/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.strategy;

import static java.util.stream.Collectors.toList;
import static org.mule.apikit.validation.Severity.ERROR;
import static org.mule.apikit.validation.Severity.WARNING;

import org.mule.apikit.validation.ApiValidationReport;
import org.mule.apikit.validation.Severity;
import org.mule.parser.service.result.DefaultParsingIssue;
import org.mule.parser.service.result.ParsingIssue;

import java.util.List;

public class ValidationReportHelper {

  public static List<ParsingIssue> errors(ApiValidationReport report) {
    return getBySeverity(report, ERROR);
  }

  public static List<ParsingIssue> warnings(ApiValidationReport report) {
    return getBySeverity(report, WARNING);
  }

  private static List<ParsingIssue> getBySeverity(ApiValidationReport report, Severity severity) {
    return report.getResults()
        .stream()
        .filter(result -> severity.equals(result.getSeverity()))
        .map(result -> new DefaultParsingIssue(result.toString())).collect(toList());
  }
}
