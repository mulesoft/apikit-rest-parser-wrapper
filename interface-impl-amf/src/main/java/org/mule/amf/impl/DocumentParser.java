/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import amf.ProfileName;
import amf.client.model.document.Document;
import amf.client.parse.Parser;
import amf.client.resolve.Resolver;
import amf.client.validate.ValidationReport;
import amf.client.validate.ValidationResult;
import amf.plugins.document.webapi.resolution.pipelines.AmfResolutionPipeline;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.amf.impl.parser.factory.AMFParserWrapper;
import org.mule.apikit.model.api.ApiReference;

import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mule.amf.impl.URIUtils.getPathAsUri;


public class DocumentParser {

  private DocumentParser() {
  }

  static Document parseFile(final AMFParserWrapper parserWrapper, final ApiReference apiRef, final boolean validate) throws ParserException {
    Parser parser = parserWrapper.getParser();
    final URI uri = getPathAsUri(apiRef);
    final String url = URLDecoder.decode(uri.toString());
    Document document = handleFuture(parser.parseFileAsync(url));
    Resolver resolver = parserWrapper.getResolver();
    document = (Document) resolver.resolve(document, AmfResolutionPipeline.EDITING_PIPELINE());

    if (validate) {
      final ValidationReport parsingReport = getParsingReport(parser, parserWrapper.getProfileName());
      if (!parsingReport.conforms()) {
        final List<ValidationResult> results = parsingReport.results();
        if (!results.isEmpty()) {
          final String message = results.get(0).message();
          throw new ParserException(message);
        }
      }
    }
    return document;
  }

  static ValidationReport getParsingReport(final Parser parser, final ProfileName profileName) throws ParserException {
    return handleFuture(parser.reportValidation(profileName));
  }

  private static <T, U> U handleFuture(CompletableFuture<T> f) throws ParserException {
    try {
      return (U) f.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new ParserException("An error happened while parsing the api. Message: " + e.getMessage(), e);
    }
  }

}
