/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.parser.factory;

import amf.apicontract.client.platform.AMFConfiguration;
import amf.apicontract.client.platform.APIConfiguration;
import amf.core.client.common.transform.PipelineId;
import amf.core.client.platform.AMFParseResult;
import amf.core.client.platform.AMFResult;
import amf.core.client.platform.config.RenderOptions;
import amf.core.client.platform.execution.ExecutionEnvironment;
import amf.core.client.platform.model.document.BaseUnit;
import amf.core.client.platform.model.document.Document;
import amf.core.client.platform.validation.AMFValidationReport;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.amf.impl.loader.ProvidedResourceLoader;
import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.api.ApiReference;
import org.yaml.builder.JsonOutputBuilder;

import java.net.URLDecoder;
import java.util.concurrent.CompletableFuture;

public class AMFParserWrapper {

  private BaseUnit model;
  private AMFConfiguration amfConfiguration;

  public AMFParserWrapper(ApiReference apiRef, ExecutionEnvironment execEnv) {
    AMFConfiguration unknownApiConfiguration = APIConfiguration
        .API()
        .withExecutionEnvironment(execEnv);

    ResourceLoader resourceLoader = apiRef.getResourceLoader().orElse(null);
    if (resourceLoader != null) {
      unknownApiConfiguration =
          unknownApiConfiguration.withResourceLoader(new ProvidedResourceLoader(apiRef.getResourceLoader().get()));
    }

    AMFParseResult amfParseResult =
        handleFuture(unknownApiConfiguration.baseUnitClient().parse(URLDecoder.decode(apiRef.getPathAsUri().toString())));
    this.model = amfParseResult.baseUnit();

    RenderOptions renderOptions = new RenderOptions()
        .withAmfJsonLdSerialization()
        .withoutSourceMaps()
        .withoutPrettyPrint()
        .withCompactUris();
    this.amfConfiguration =
        APIConfiguration.fromSpec(amfParseResult.sourceSpec()).withExecutionEnvironment(execEnv).withRenderOptions(renderOptions);

    if (resourceLoader != null) {
      this.amfConfiguration =
          this.amfConfiguration.withResourceLoader(new ProvidedResourceLoader(apiRef.getResourceLoader().get()));
    }
  }

  public Document parseApi() throws ParserException {
    AMFResult result = amfConfiguration.baseUnitClient().transform(model, PipelineId.Editing());
    this.model = null;
    return (Document) result.baseUnit();
  }

  public AMFValidationReport getParsingReport(Document resolvedDoc) throws ParserException {
    return handleFuture(amfConfiguration.baseUnitClient().validate(resolvedDoc));
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

  public String renderApi(Document document) {
    return amfConfiguration.baseUnitClient().render(document);
  }

  public <W> void renderApi(Document document, JsonOutputBuilder<W> wJsonOutputBuilder) {
    amfConfiguration.baseUnitClient().renderGraphToBuilder(document, wJsonOutputBuilder);
  }

  public AMFConfiguration getAmfConfiguration() {
    return amfConfiguration;
  }
}
