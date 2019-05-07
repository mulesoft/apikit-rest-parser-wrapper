/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import java.util.List;

/**
 * Represents a parsing error, with description and a list of children errors
 */
public class CompositeParsingError implements ParsingError {

  private final String description;
  private final List<DefaultParsingError> errors;

  public CompositeParsingError(String description, List<DefaultParsingError> errors) {
    this.description = description;
    this.errors = errors;
  }

  @Override
  public String cause() {
    StringBuilder builder = new StringBuilder(description);
    builder.append(":");
    errors.forEach(error -> {
      builder.append("\n");
      builder.append(error.cause());
    });
    return builder.toString();
  }
}
