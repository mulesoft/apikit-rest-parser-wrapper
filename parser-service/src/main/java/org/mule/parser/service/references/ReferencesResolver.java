/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.references;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;

import java.util.List;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.result.ParseResult;
import org.mule.parser.service.strategy.AMFParsingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferencesResolver {

  private ParseResult amfParseResult;
  private static final AMFParsingStrategy amfParsingStrategy = new AMFParsingStrategy(false);
  private static final Logger LOGGER = LoggerFactory.getLogger(ReferencesResolver.class.getName());

  public ReferencesResolver() {
  }

  public ReferencesResolver(ParseResult amfParseResult) {
    this.amfParseResult = amfParseResult;
  }

  public List<String> getReferences(ApiReference reference) {
    if (amfParseResult == null) {
      this.amfParseResult = amfParsingStrategy.parse(reference);
    }
    return getReferences(amfParseResult);
  }

  private List<String> getReferences(ParseResult amfParseResult) {
    if (!amfParseResult.success()) {
      String message = amfParseResult.getErrors().stream().map(e -> e.toString()).collect(joining("\n"));
      LOGGER.error(message);
      return emptyList();
    }
    return amfParseResult.get().getAllReferences();
  }

}
