/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.references;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.result.ParseResult;
import org.mule.parser.service.strategy.AMFParsingStrategy;

public class ReferencesResolver {

  private ParseResult amfParseResult;
  private static final AMFParsingStrategy amfParsingStrategy = new AMFParsingStrategy(false);

  public ReferencesResolver() {}

  public ReferencesResolver(ParseResult amfParseResult) {
    this.amfParseResult = amfParseResult;
  }

  public List<String> getReferences(ApiReference reference) {
    if (amfParseResult != null) {
      return getReferences(amfParseResult);
    }
    return getReferences(amfParsingStrategy.parse(reference));
  }

  private List<String> getReferences(ParseResult amfParseResult) {
    return amfParseResult.get() != null ? amfParseResult.get().getAllReferences() : emptyList();
  }

  public void setExecutor(ScheduledExecutorService executor) {
    amfParsingStrategy.setExecutor(executor);
  }
}
