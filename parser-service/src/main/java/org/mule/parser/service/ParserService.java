/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import static org.mule.parser.service.ParserMode.AMF;
import static org.mule.parser.service.ParserMode.AUTO;
import static org.mule.parser.service.ParserMode.RAML;

import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.result.ParseResult;

import java.util.Optional;

public class ParserService {

  private static final String MULE_APIKIT_PARSER = "mule.apikit.parser";

  public ParseResult parse(ApiReference ref) {
    return parse(ref, AUTO);
  }

  public ParseResult parse(ApiReference ref, ParserMode parserConfig) {
    ParserMode parser = getOverrideParserConfig().orElse(parserConfig);
    return parser.getStrategy().parse(ref);
  }

  private Optional<ParserMode> getOverrideParserConfig() {
    String parserType = System.getProperty(MULE_APIKIT_PARSER);
    if (parserType == null) {
      return Optional.empty();
    }
    if (AMF.name().equals(parserType)) {
      return Optional.of(AMF);
    }
    if (RAML.name().equals(parserType)) {
      return Optional.of(RAML);
    }
    return Optional.of(AUTO);
  }
}
