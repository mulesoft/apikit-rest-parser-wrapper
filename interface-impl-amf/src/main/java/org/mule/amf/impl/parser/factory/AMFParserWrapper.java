/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.parser.factory;

import amf.apicontract.client.platform.AMFBaseUnitClient;
import amf.apicontract.client.platform.AMFConfiguration;
import amf.apicontract.client.platform.APIConfiguration;
import amf.core.client.common.transform.PipelineId;
import amf.core.client.platform.AMFParseResult;
import amf.core.client.platform.config.RenderOptions;
import amf.core.client.platform.execution.ExecutionEnvironment;
import amf.core.client.platform.model.document.BaseUnit;
import amf.core.client.platform.model.document.Document;
import amf.core.client.platform.resource.ResourceLoader;
import amf.core.client.platform.validation.AMFValidationReport;
import amf.core.internal.remote.Spec;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.amf.impl.loader.ProvidedResourceLoader;
import org.mule.apikit.loader.ApiSyncResourceLoader;
import org.mule.apikit.model.api.ApiReference;
import org.yaml.builder.JsonOutputBuilder;

import java.net.URLDecoder;
import java.util.concurrent.CompletableFuture;

public class AMFParserWrapper {


  private final BaseUnit model;
  private final AMFBaseUnitClient client;
  private final Spec spec;


  public AMFParserWrapper(ApiReference apiRef, ExecutionEnvironment execEnv) {
    AMFConfiguration amfConfiguration = APIConfiguration
        .API()
        .withExecutionEnvironment(execEnv);

    if (apiRef.getResourceLoader().isPresent()) {
      amfConfiguration = amfConfiguration.withResourceLoader(new ProvidedResourceLoader(apiRef.getResourceLoader().get()));
    }

    AMFParseResult amfParseResult = handleFuture(amfConfiguration.baseUnitClient()
        .parse(URLDecoder.decode(apiRef.getPathAsUri().toString())));
    this.model = amfParseResult.baseUnit();
    this.spec = amfParseResult.sourceSpec();
    this.client = APIConfiguration.fromSpec(spec).baseUnitClient();
  }

  private ResourceLoader getResourceLoader() {

    return null;
  }

  public Document parseApi() throws ParserException {
    client.transform(model, PipelineId.Editing());
    return (Document) client.transform(model, PipelineId.Editing()).baseUnit();
  }


  public AMFValidationReport getParsingReport(Document resolvedDoc) throws ParserException {
    AMFValidationReport report = handleFuture(client.validate(resolvedDoc));

    return report;
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
    return getRenderClient().render(document);
  }

  public AMFBaseUnitClient getRenderClient() {
    RenderOptions renderOptions = new RenderOptions()
        .withoutSourceMaps()
        .withoutPrettyPrint()
        .withCompactUris();
    return APIConfiguration.fromSpec(spec).withRenderOptions(renderOptions).baseUnitClient();
  }

  public <W> void renderApi(Document document, JsonOutputBuilder<W> wJsonOutputBuilder) {
    getRenderClient().renderGraphToBuilder(document, wJsonOutputBuilder);
  }

  public Spec getSpec() {
    return spec;
  }
}
