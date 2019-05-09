/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.strategy;

import static org.mule.apikit.model.ApiVendor.RAML_08;
import static org.mule.parser.service.strategy.ValidationReportHelper.errors;
import static org.mule.parser.service.strategy.ValidationReportHelper.warnings;

import org.mule.apikit.ApiParser;
import org.mule.apikit.implv1.ParserWrapperV1;
import org.mule.apikit.implv2.ParserWrapperV2;
import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.validation.ApiValidationReport;
import org.mule.parser.service.result.DefaultParseResult;
import org.mule.parser.service.result.ExceptionParseResult;
import org.mule.parser.service.result.ParseResult;

public class RamlParsingStrategy implements ParsingStrategy {

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

  public ApiParser create(ApiReference ref) {
    String path = ref.getLocation();
    ResourceLoader apiLoader = ref.getResourceLoader().orElse(null);

    // TODO consider whether to use v1 or v2 when vendor is raml 0.8 (ParserV2Utils.useParserV2)
    if (RAML_08.equals(ref.getVendor())) {
      return createRamlParserWrapperV1(path, apiLoader);
    } else {
      return createRamlParserWrapperV2(path, apiLoader);
    }
  }

  private ParserWrapperV1 createRamlParserWrapperV1(String path, ResourceLoader apiLoader) {
    return apiLoader != null
      ? new ParserWrapperV1(path, ParserWrapperV1.getResourceLoaderForPath(path), apiLoader::getResourceAsStream)
      : new ParserWrapperV1(path);
  }

  private ParserWrapperV2 createRamlParserWrapperV2(String path, ResourceLoader apiLoader) {
    return apiLoader != null
      ? new ParserWrapperV2(path, ParserWrapperV2.getResourceLoaderForPath(path), apiLoader::getResourceAsStream)
      : new ParserWrapperV2(path);
  }
}
