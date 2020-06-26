/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import amf.MessageStyle;
import amf.ProfileName;
import amf.client.AMF;
import amf.client.model.document.Document;
import amf.client.parse.Parser;
import amf.client.resolve.Resolver;
import amf.client.validate.ValidationReport;
import amf.plugins.document.webapi.resolution.pipelines.AmfResolutionPipeline;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.amf.impl.parser.factory.AMFParserWrapper;

import java.net.URI;
import java.net.URLDecoder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class DocumentParser {

  private DocumentParser() {
  }

  public static Document parseFile(final Parser parser, final Resolver resolver, final URI uri) throws ParserException {
    Document document = getDocument(parser, uri);
    return (Document) resolver.resolve(document, AmfResolutionPipeline.EDITING_PIPELINE());
  }

  static Document parseFile(final AMFParserWrapper parserWrapper, final URI uri) throws ParserException {
    Document document = getDocument(parserWrapper.getParser(), uri);
    Resolver resolver = parserWrapper.getResolver();
    return (Document) resolver.resolve(document, AmfResolutionPipeline.EDITING_PIPELINE());
  }

  private static Document getDocument(Parser parser, URI uri) {
    final String url = URLDecoder.decode(uri.toString());
    return handleFuture(parser.parseFileAsync(url));
  }

  static ValidationReport getParsingReport(Document resolvedDoc, ProfileName profileName, MessageStyle messageStyle) throws ParserException {
    return handleFuture(AMF.validateResolved(resolvedDoc, profileName, messageStyle));
  }

  private static <T, U> U handleFuture(CompletableFuture<T> f) throws ParserException {
    try {
      return (U) f.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new ParserException("An error happened while parsing the api. Message: " + e.getMessage(), e);
    }
  }

}
