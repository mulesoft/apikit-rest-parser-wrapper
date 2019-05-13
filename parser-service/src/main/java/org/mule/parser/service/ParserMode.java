/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import org.mule.parser.service.strategy.AMFParsingStrategy;
import org.mule.parser.service.strategy.ParsingStrategy;
import org.mule.parser.service.strategy.RamlParsingStrategy;
import org.mule.parser.service.strategy.WithFallbackParsingStrategy;

public enum ParserMode {

  AMF(new AMFParsingStrategy()),
  RAML(new RamlParsingStrategy()),
  AUTO(new WithFallbackParsingStrategy());

  private final ParsingStrategy strategy;

  ParserMode(ParsingStrategy strategy) {
    this.strategy = strategy;
  }

  public ParsingStrategy getStrategy() {
    return strategy;
  }
}
