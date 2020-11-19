/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.result.internal;

import org.mule.parser.service.result.ParsingIssue;
import org.mule.parser.service.result.ParsingIssueCode;

import static org.mule.parser.service.result.ParsingIssueCode.EXCEPTION;

/**
 * Represents a simple parsing error with cause message
 */
public class DefaultParsingIssue implements ParsingIssue {

  private final String cause;

  public DefaultParsingIssue(String cause) {
    this.cause = cause;
  }

  @Override
  public String cause() {
    return cause;
  }

  @Override
  public ParsingIssueCode code() {
    return EXCEPTION;
  }

  @Override
  public String toString() {
    return cause;
  }
}
