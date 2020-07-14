/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.parser.factory;

import amf.MessageStyle;
import amf.ProfileName;
import amf.client.AMF;
import amf.client.environment.Environment;
import amf.client.model.document.Document;
import amf.client.parse.Parser;
import amf.client.resolve.Resolver;
import amf.client.validate.ValidationReport;
import amf.plugins.document.webapi.resolution.pipelines.AmfResolutionPipeline;
import org.mule.amf.impl.exceptions.ParserException;

import java.net.URI;
import java.net.URLDecoder;
import java.util.concurrent.CompletableFuture;

public class AMFParserWrapper {

  private Environment environment;
  private Parser parser;
  private Resolver resolver;
  private ProfileName profileName;
  private MessageStyle messageStyle;

  public AMFParserWrapper(Environment environment, Parser parser, Resolver resolver, ProfileName profileName,
                          MessageStyle messageStyle) {
    this.environment = environment;
    this.parser = parser;
    this.resolver = resolver;
    this.profileName = profileName;
    this.messageStyle = messageStyle;
  }

  public Document parseApi(final URI uri) throws ParserException {
    final String url = URLDecoder.decode(uri.toString());
    Document document = handleFuture(parser.parseFileAsync(url));
    return (Document) resolver.resolve(document, AmfResolutionPipeline.EDITING_PIPELINE());
  }

  public ValidationReport getParsingReport(Document resolvedDoc) throws ParserException {
    return handleFuture(AMF.validateResolved(resolvedDoc, profileName, messageStyle, environment));
  }

  private <T, U> U handleFuture(CompletableFuture<T> f) throws ParserException {
    try {
      return (U) f.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw getParseException(e);
    } catch (Exception e) {
      throw getParseException(e);
    }
  }

  private static ParserException getParseException(Exception e) {
    throw new ParserException("An error happened while parsing the api. Message: " + e.getMessage(), e);
  }
}
