/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import org.mule.amf.impl.AMFParser;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.apikit.implv1.ParserWrapperV1;
import org.mule.apikit.implv2.ParserWrapperV2;
import org.mule.apikit.ApiParser;
import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.api.ApiRef;
import org.mule.apikit.validation.ApiValidationReport;
import org.mule.apikit.validation.ExceptionApiValidationResult;
import org.mule.apikit.validation.ApiValidationResult;
import org.mule.apikit.validation.Severity;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mule.apikit.model.ApiVendor.RAML_08;
import static org.mule.apikit.validation.Severity.WARNING;
import static org.mule.parser.service.ParserConfiguration.AMF;
import static org.mule.parser.service.ParserConfiguration.AUTO;
import static org.mule.parser.service.ParserConfiguration.RAML;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserService {

  private static final String MULE_APIKIT_PARSER = "mule.apikit.parser";

  private static final Logger LOGGER = LoggerFactory.getLogger(ParserService.class);

  private final List<ParsingError> parsingErrors = new ArrayList<>();

  public List<ParsingError> getParsingErrors() {
    return parsingErrors;
  }

  public ApiParser getParser(ApiRef apiRef) {
    return getParser(apiRef, AUTO);
  }

  public ApiParser getParser(ApiRef apiRef, ParserConfiguration parserType) {
    ParserConfiguration overridden = getOverriddenParserType();
    if (overridden != AUTO) {
      return getParserFor(apiRef, overridden);
    } else {
      return getParserFor(apiRef, parserType);
    }
  }

  private ParserConfiguration getOverriddenParserType() {
    final String parserValue = System.getProperty(MULE_APIKIT_PARSER);
    if (AMF.name().equals(parserValue)) {
      return AMF;
    }
    if (RAML.name().equals(parserValue)) {
      return RAML;
    }
    return AUTO;
  }

  private ApiParser getParserFor(ApiRef apiRef, ParserConfiguration parserType) {
    ApiParser apiParser;
    try {
      if (parserType == RAML) {
        apiParser = createRamlParserWrapper(apiRef);
      } else {
        apiParser = AMFParser.create(apiRef, false);
      }

      final ApiValidationReport validationReport = apiParser.validate();

      if (validationReport.conforms()) {
        return apiParser;
      } else {
        List<ApiValidationResult> errorsFound = validationReport.getResults();
        CompositeParsingError validationError =
            new CompositeParsingError(format("Validation failed using parser: %s, in file: %s",
                                             apiParser.getParserType(), apiRef.getLocation()),
                                              errorsFound.stream().map(e -> new DefaultParsingError(e.getMessage()))
                                                .collect(toList()));
        parsingErrors.add(validationError);
        return applyFallback(apiRef, parserType, errorsFound);
      }
    } catch (ParserServiceException | ParserException e) {
      throw new ParserServiceException(e);
    } catch (Exception e) {
      parsingErrors.add(new DefaultParsingError(e.getMessage()));
      return applyFallback(apiRef, parserType, singletonList(new ExceptionApiValidationResult(e)));
    }
  }

  // Only fallback if is RAML
  private ApiParser applyFallback(ApiRef apiRef, ParserConfiguration parserType, List<ApiValidationResult> errorsFound)
      throws ParserServiceException {
    if (parserType == AUTO) {
      final ApiParser fallbackParser = createRamlParserWrapper(apiRef);
      if (fallbackParser.validate().conforms()) {
        logErrors(errorsFound, WARNING);
        return fallbackParser;
      } else {
        CompositeParsingError fallbackError =
            new CompositeParsingError(format("Validation failed using fallback parser: %s, in file: %s",
                                             fallbackParser.getParserType(), apiRef.getLocation()),
                                              fallbackParser.validate().getResults().stream()
                                              .map(e -> new DefaultParsingError(e.getMessage())).collect(toList()));
        parsingErrors.add(fallbackError);
      }
    }
    logErrors(errorsFound);
    throw new ParserServiceException(buildErrorMessage(errorsFound));
  }

  private void logErrors(List<ApiValidationResult> validationResults) {
    validationResults.forEach(error -> logError(error, error.getSeverity()));
  }

  private void logErrors(List<ApiValidationResult> validationResults, Severity overridenSeverity) {
    validationResults.forEach(error -> logError(error, overridenSeverity));
  }

  private void logError(ApiValidationResult error, Severity severity) {
    if (severity == Severity.INFO) {
      LOGGER.info(error.getMessage());
    } else if (severity == WARNING) {
      LOGGER.warn(error.getMessage());
    } else {
      LOGGER.error(error.getMessage());
    }
  }

  private static String buildErrorMessage(List<ApiValidationResult> validationResults) {
    final StringBuilder message = new StringBuilder("Invalid API descriptor -- errors found: ");
    message.append(validationResults.size()).append("\n\n");
    for (ApiValidationResult error : validationResults) {
      message.append(error.getMessage()).append("\n");
    }
    return message.toString();
  }

  private static ApiParser createRamlParserWrapper(ApiRef apiRef) {
    final String path = apiRef.getLocation();

    final ResourceLoader apiLoader = apiRef.getResourceLoader().orElse(null);

    // TODO consider whether to use v1 or v2 when vendor is raml 0.8 (ParserV2Utils.useParserV2)
    if (RAML_08.equals(apiRef.getVendor())) {
      return createRamlParserWrapperV1(path, apiLoader);
    } else {
      return createRamlParserWrapperV2(path, apiLoader);
    }
  }

  private static ParserWrapperV1 createRamlParserWrapperV1(String path, ResourceLoader apiLoader) {
    return apiLoader != null
        ? new ParserWrapperV1(path, ParserWrapperV1.getResourceLoaderForPath(path), apiLoader::getResourceAsStream)
        : new ParserWrapperV1(path);
  }

  private static ParserWrapperV2 createRamlParserWrapperV2(String path, ResourceLoader apiLoader) {
    return apiLoader != null
        ? new ParserWrapperV2(path, ParserWrapperV2.getResourceLoaderForPath(path), apiLoader::getResourceAsStream)
        : new ParserWrapperV2(path);
  }
}
