/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.Encoding;
import amf.client.model.domain.Example;
import amf.client.model.domain.NodeShape;
import amf.client.model.domain.Payload;
import amf.client.model.domain.PropertyShape;
import amf.client.model.domain.Shape;
import amf.client.model.domain.UnionShape;
import amf.client.validate.PayloadValidator;
import amf.client.validate.ValidationReport;
import org.apache.commons.collections.CollectionUtils;
import org.mule.amf.impl.parser.rule.ApiValidationResultImpl;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.apikit.validation.ApiValidationResult;
import org.mule.apikit.validation.ExceptionApiValidationResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableList.of;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.mule.amf.impl.model.MediaType.getMimeTypeForValue;

public class MimeTypeImpl implements MimeType {

  private final Payload payload;
  private final Shape shape;
  private final Map<String, PayloadValidator> payloadValidatorMap = new HashMap<>();
  private final String defaultMediaType;
  private Map<String, List<Parameter>> formParameters;

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
    if (isNotEmpty(formParameters)) {
      return formParameters;
    }

    String mediaType = payload.mediaType().value();

    if (mediaType.startsWith("multipart/") || mediaType.equals("application/x-www-form-urlencoded")) {

      if (!(shape instanceof NodeShape)) {
        return emptyMap();
      }
      NodeShape nodeShape = (NodeShape) shape;

      Map<String, Set<String>> formParametersEncoding = getFormParametersEncoding();


      formParameters = new LinkedHashMap<>();
      for (PropertyShape propertyShape : nodeShape.properties()) {
        String propertyName = propertyShape.name().value();
        formParameters.put(propertyName,
                           singletonList(new ParameterImpl(propertyShape, formParametersEncoding.get(propertyName))));
      }

      return formParameters;
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

    if (shape == null) {
      List<Example> examplesList = payload.examples();
      if (CollectionUtils.isNotEmpty(examplesList)) {
        return getExampleValueByMediaType(examplesList.get(0));
      }
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
    String mimeType = getMimeTypeForValue(payload);

    PayloadValidator payloadValidator = payloadValidatorMap.computeIfAbsent(mimeType,
                                                                            payloadMimeType -> getPayloadValidator(payloadMimeType)
                                                                                .orElse(null));

    if (payloadValidator != null) {
      return mapToValidationResult(payloadValidator.syncValidate(mimeType, payload));
    }
    return of(
              new ExceptionApiValidationResult(new RuntimeException(format("Validator not found for %s", mimeType))));
  }

  private Optional<PayloadValidator> getPayloadValidator(String mediaType) {
    return ((AnyShape) shape).payloadValidator(mediaType);
  }

  private static List<ApiValidationResult> mapToValidationResult(ValidationReport validationReport) {
    if (validationReport.conforms()) {
      return emptyList();
    }
    return validationReport.results().stream().map(ApiValidationResultImpl::new)
        .collect(toList());
  }

  private Map<String, Set<String>> getFormParametersEncoding() {
    Map<String, Set<String>> formParametersEncoding = new LinkedHashMap<>();
    List<Encoding> encodingList = payload.encoding();
    if (isNotEmpty(encodingList)) {
      encodingList.forEach(e -> formParametersEncoding.put(e.propertyName().value(), getContentTypeSet(e.contentType().value())));
    }
    return formParametersEncoding;
  }

  private Set<String> getContentTypeSet(String contentTypeCSV) {
    String[] contentTypes = contentTypeCSV.split(",");
    Set<String> contentTypeSet = new HashSet<>();
    for (String contentType : contentTypes) {
      contentTypeSet.add(contentType.trim());
    }
    return contentTypeSet;
  }

}
