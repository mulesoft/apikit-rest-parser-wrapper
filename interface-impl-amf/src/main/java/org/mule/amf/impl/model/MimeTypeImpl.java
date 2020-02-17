/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.Example;
import amf.client.model.domain.NodeShape;
import amf.client.model.domain.Payload;
import amf.client.model.domain.PropertyShape;
import amf.client.model.domain.Shape;
import amf.client.model.domain.UnionShape;
import amf.client.validate.PayloadValidator;
import amf.client.validate.ValidationReport;
import org.mule.amf.impl.parser.rule.ApiValidationResultImpl;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.apikit.validation.ApiValidationResult;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.mule.amf.impl.model.MediaType.getMimeTypeForValue;

public class MimeTypeImpl implements MimeType {

  private final Payload payload;
  private final Shape shape;
  private final Map<String, Optional<PayloadValidator>> payloadValidatorMap = new HashMap<>();
  private final String defaultMediaType;

  public MimeTypeImpl(final Payload payload) {
    this.payload = payload;
    this.shape = payload.schema();
    this.defaultMediaType = this.payload.mediaType().option().orElse(null);
  }

  @Override
  public Object getCompiledSchema() {
    return null;
  }

  @Override
  public String getSchema() {
    if (shape.getClass() == AnyShape.class)
      return null;

    if (shape instanceof AnyShape)
      return ((AnyShape) shape).buildJsonSchema();

    return null;
  }

  @Override
  public Map<String, List<Parameter>> getFormParameters() {
    String mediaType = payload.mediaType().value();

    if (mediaType.startsWith("multipart/") || mediaType.equals("application/x-www-form-urlencoded")) {

      if (!(shape instanceof NodeShape)) {
        return emptyMap();
      }
      NodeShape nodeShape = (NodeShape) shape;

      Map<String, List<Parameter>> parameters = new LinkedHashMap<>();

      for (PropertyShape propertyShape : nodeShape.properties()) {
        parameters.put(propertyShape.name().value(), singletonList(new ParameterImpl(propertyShape)));
      }

      return parameters;
    }

    return emptyMap();
  }

  @Override
  public String getType() {
    return payload.mediaType().value();
  }

  @Override
  public String getExample() {
    if (shape instanceof UnionShape) {
      final UnionShape unionShape = (UnionShape) shape;
      for (Shape shape : unionShape.anyOf()) {
        if (shape instanceof AnyShape) {
          final String example = getExampleFromAnyShape((AnyShape) shape);
          if (example != null)
            return example;
        }
      }
    }

    if (shape instanceof AnyShape) {
      return getExampleFromAnyShape((AnyShape) shape);
    }

    return null;
  }

  private String getExampleFromAnyShape(AnyShape anyShape) {
    final Optional<Example> trackedExample = anyShape.trackedExample(payload.id());

    if (trackedExample.isPresent()) {
      final Example example = trackedExample.get();
      if (example.value().nonNull())
        return getExampleValueByMediaType(example);
    }

    return anyShape.examples().stream().filter(example -> example.value().value() != null)
            .map(example -> getExampleValueByMediaType(example))
            .findFirst()
            .orElse(null);
  }

  private String getExampleValueByMediaType(Example example) {
    String mimeType = firstNonNull(getType(), defaultMediaType);
    switch (mimeType) {
      case MediaType.APPLICATION_JSON:
        return example.toJson();
      case MediaType.APPLICATION_YAML:
        return example.toYaml();
      default:
        return example.value().value();
    }
  }

  @Override
  public Object getInstance() {
    return null;
  }

  @Override
  public List<ApiValidationResult> validate(String payload) {
    final String mimeType = getMimeTypeForValue(payload);

    Optional<PayloadValidator> payloadValidator;
    if (!payloadValidatorMap.containsKey(mimeType)) {
      payloadValidator = getPayloadValidator(mimeType);

      if (!payloadValidator.isPresent()) {
        payloadValidator = getPayloadValidator(defaultMediaType);
      }

      payloadValidatorMap.put(mimeType, payloadValidator);
    } else {
      payloadValidator = payloadValidatorMap.get(mimeType);
    }

    if (payloadValidator.isPresent()) {
      final ValidationReport result;
      try {
        result = payloadValidator.get().validate(mimeType, payload).get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException("Unexpected Error validating payload", e);
      }
      return mapToValidationResult(result);
    } else {
      throw new RuntimeException("Unexpected Error validating payload");
    }
  }

  private Optional<PayloadValidator> getPayloadValidator(String mediaType) {
    return ((AnyShape) shape).payloadValidator(mediaType);
  }

  private static List<ApiValidationResult> mapToValidationResult(ValidationReport validationReport) {
    if (validationReport.conforms())
      return emptyList();
    else
      return validationReport.results().stream().map(ApiValidationResultImpl::new).collect(toList());
  }
}
