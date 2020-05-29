/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import amf.ProfileName;
import amf.client.environment.Environment;
import amf.client.execution.ExecutionEnvironment;
import amf.client.model.document.Document;
import amf.client.parse.Parser;
import amf.client.resolve.Resolver;
import amf.client.validate.ValidationReport;
import amf.client.validate.ValidationResult;
import amf.plugins.document.webapi.resolution.pipelines.AmfResolutionPipeline;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.api.ApiReference;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mule.amf.impl.URIUtils.getPathAsUri;
import static org.mule.amf.impl.URIUtils.uriToPath;
import static org.mule.amf.impl.VendorUtils.getParserByVendor;
import static org.mule.amf.impl.VendorUtils.getProfileNameByVendor;
import static org.mule.amf.impl.VendorUtils.getResolverByVendor;


public class DocumentParser {

  private DocumentParser() {
  }

  public static Document parseFile(final Parser parser, final ApiReference apiRef, final boolean validate) throws ParserException {
    final URI uri = getPathAsUri(apiRef);
    final ApiVendor apiVendor = apiRef.getVendor();
    return parseFile(parser, uri, apiVendor, validate);
  }

  private static Document parseFile(final Parser parser, final URI uri, final ApiVendor apiVendor, final boolean validate)
          throws ParserException {
    Document document = parseFile(parser, uriToPath(uri));
    Resolver resolver = getResolverByVendor(apiVendor);
    document = (Document) resolver.resolve(document, AmfResolutionPipeline.EDITING_PIPELINE());

    if (validate) {
      final ValidationReport parsingReport = DocumentParser.getParsingReport(parser, apiVendor);
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

  private static Document parseFile(final Parser parser, final String url) throws ParserException {
    return handleFuture(parser.parseFileAsync(url));
  }

  static Parser getParserForApi(final ApiReference apiRef, Environment environment, ExecutionEnvironment executionEnvironment) {
    return getParserByVendor(apiRef, environment);
  }

  static ValidationReport getParsingReport(final Parser parser, final ApiVendor apiVendor) throws ParserException {
    final ProfileName profile = getProfileNameByVendor(apiVendor);
    return handleFuture(parser.reportValidation(profile));
  }

  private static <T, U> U handleFuture(CompletableFuture<T> f) throws ParserException {
    try {
      return (U) f.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new ParserException("An error happened while parsing the api. Message: " + e.getMessage(), e);
    }
  }

}
