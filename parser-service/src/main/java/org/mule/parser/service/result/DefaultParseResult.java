/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.result;

import org.mule.apikit.model.ApiSpecification;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class DefaultParseResult implements ParseResult {

  private final ApiSpecification api;
  private final List<ParsingIssue> errors;
  private final List<ParsingIssue> warns;

  public DefaultParseResult(ApiSpecification api, List<ParsingIssue> errors, List<ParsingIssue> warns) {
    this.api = api;
    this.errors = ImmutableList.copyOf(errors);
    this.warns = warns;
  }

  @Override
  public ApiSpecification get() {
    return api;
  }

  @Override
  public boolean success() {
    return errors.isEmpty();
  }

  @Override
  public List<ParsingIssue> getErrors() {
    return errors;
  }

  @Override
  public List<ParsingIssue> getWarnings() {
    return warns;
  }

}
