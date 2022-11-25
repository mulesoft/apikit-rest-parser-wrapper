/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.result;

import org.mule.apikit.model.ApiSpecification;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class ExceptionParseResult implements ParseResult {

  private final ParsingIssue error;

  public ExceptionParseResult(Exception e) {
    StringWriter writer = new StringWriter();
    e.printStackTrace(new PrintWriter(writer));
    this.error = new ExceptionParsingIssue("Error while parsing API: " + e.getMessage(), writer.toString());
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
