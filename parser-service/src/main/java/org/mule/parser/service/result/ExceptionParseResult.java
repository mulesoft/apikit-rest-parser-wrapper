/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.result;

import static java.util.Collections.*;

import org.mule.apikit.model.ApiSpecification;

import java.util.List;

public class ExceptionParseResult implements ParseResult {

  private final DefaultParsingIssue error;

  public ExceptionParseResult(Exception e) {
    this.error = new DefaultParsingIssue("Error while parsing API: " + e.getMessage());
  }

  @Override
  public ApiSpecification get() {
    return null;
  }

  @Override
  public boolean success() {
    return false;
  }

  @Override
  public List<ParsingIssue> getErrors() {
    return singletonList(error);
  }

  @Override
  public List<ParsingIssue> getWarnings() {
    return emptyList();
  }
}
