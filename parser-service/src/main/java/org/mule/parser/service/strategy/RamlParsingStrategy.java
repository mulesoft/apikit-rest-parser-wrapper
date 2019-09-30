/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.strategy;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.mule.apikit.model.ApiVendor.RAML_08;
import static org.mule.parser.service.strategy.ValidationReportHelper.errors;
import static org.mule.parser.service.strategy.ValidationReportHelper.warnings;

import java.util.List;
import java.util.stream.Collectors;
import org.mule.apikit.ApiParser;
import org.mule.apikit.implv1.ParserWrapperV1;
import org.mule.apikit.implv2.ParserWrapperV2;
import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.validation.ApiValidationReport;
import org.mule.parser.service.result.DefaultParseResult;
import org.mule.parser.service.result.ExceptionParseResult;
import org.mule.parser.service.result.ParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RamlParsingStrategy implements ParsingStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(RamlParsingStrategy.class.getName());
  private final AMFParsingStrategy parsingStrategy = new AMFParsingStrategy();

  @Override
  public ParseResult parse(ApiReference ref) {
    try {
      ApiParser parser = create(ref);
      ApiValidationReport report = parser.validate();
      return new DefaultParseResult(report.conforms() ? parser.parse() : null, errors(report), warnings(report));
    } catch (Exception e) {
      return new ExceptionParseResult(e);
    }
  }

  private List<String> getReferences(ApiReference ref) {
    ParseResult result = parsingStrategy.parse(ref);
    if (!result.success()) {
      String message = result.getErrors().stream().map(e -> e.toString()).collect(joining("\n"));
      LOGGER.error(message);
      return emptyList();
    }
    return result.get().getAllReferences();
  }

  public ApiParser create(ApiReference ref) {
    String path = ref.getLocation();
    ResourceLoader apiLoader = ref.getResourceLoader().orElse(null);

    List<String> references = getReferences(ref);
    if (RAML_08.equals(ref.getVendor())) {
      return createParserV1(path, apiLoader, references);
    } else {
      return createRamlV2(path, apiLoader, references);
    }
  }

  private ParserWrapperV1 createParserV1(String path, ResourceLoader loader, List<String> refs) {
    return loader != null ?
        new ParserWrapperV1(path, singletonList(loader::getResourceAsStream), refs) :
        new ParserWrapperV1(path, refs);
  }

  private ParserWrapperV2 createRamlV2(String path, ResourceLoader loader, List<String> refs) {
    return loader != null ?
        new ParserWrapperV2(path, singletonList(loader::getResourceAsStream), refs) :
        new ParserWrapperV2(path, refs);
  }
}
