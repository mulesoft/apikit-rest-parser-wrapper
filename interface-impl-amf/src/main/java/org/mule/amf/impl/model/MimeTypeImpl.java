/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.apicontract.client.platform.AMFConfiguration;
import amf.apicontract.client.platform.model.domain.Encoding;
import amf.apicontract.client.platform.model.domain.Payload;
import amf.core.client.common.validation.ValidationMode;
import amf.core.client.platform.model.domain.PropertyShape;
import amf.core.client.platform.model.domain.Shape;
import amf.core.client.platform.validation.AMFValidationReport;
import amf.core.client.platform.validation.payload.AMFShapePayloadValidator;
import amf.shapes.client.platform.model.domain.AnyShape;
import amf.shapes.client.platform.model.domain.Example;
import amf.shapes.client.platform.model.domain.NodeShape;
import amf.shapes.client.platform.model.domain.UnionShape;
import amf.xml.client.platform.plugin.XmlValidationPlugin;
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
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.ImmutableList.of;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.mule.amf.impl.model.MediaType.getMimeTypeForValue;

public class MimeTypeImpl implements MimeType {

  private final Payload payload;
  private final Shape shape;
  private final Map<String, AMFShapePayloadValidator> payloadValidatorMap = new ConcurrentHashMap<>();
  private final String defaultMediaType;
  private final AMFConfiguration amfConfiguration;
  private Map<String, List<Parameter>> formParameters;

  public MimeTypeImpl(final Payload payload, AMFConfiguration amfConfiguration) {
    this.payload = payload;
    this.shape = payload.schema();
    this.defaultMediaType = this.payload.mediaType().option().orElse(null);
    this.amfConfiguration = amfConfiguration;
  }

  @Override
  public Object getCompiledSchema() {
    return null;
  }

  @Override
  public String getSchema() {
    if (shape.getClass() == AnyShape.class)
      return null;

    if (shape instanceof AnyShape) {
      return amfConfiguration.elementClient().buildJsonSchema((AnyShape) shape);
    }

    return null;
  }

  @Override
  public Map<String, List<Parameter>> getFormParameters() {
    if (isNotEmpty(formParameters)) {
      return formParameters;
    }

    String mediaType = payload.mediaType().value();

    if (mediaType.startsWith("multipart/form-data") || mediaType.equals("application/x-www-form-urlencoded")) {

      if (!(shape instanceof NodeShape)) {
        return emptyMap();
      }
      NodeShape nodeShape = (NodeShape) shape;

      Map<String, Set<String>> formParametersEncoding = getFormParametersEncoding();


      formParameters = new LinkedHashMap<>();
      for (PropertyShape propertyShape : nodeShape.properties()) {
        String propertyName = propertyShape.name().value();
        formParameters.put(propertyName,
                           singletonList(new ParameterImpl(propertyShape, formParametersEncoding.get(propertyName),
                                                           amfConfiguration)));
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
      if (isNotEmpty(examplesList)) {
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
    if (MediaType.APPLICATION_JSON.equals(mimeType) || MediaType.APPLICATION_YAML.equals(mimeType)) {
      return amfConfiguration.elementClient().renderExample(example, mimeType);
    } else {
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

    AMFShapePayloadValidator payloadValidator = payloadValidatorMap.computeIfAbsent(mimeType,
                                                                                    payloadMimeType -> getPayloadValidator(payloadMimeType));

    if (payloadValidator != null) {
      return mapToValidationResult(payloadValidator.syncValidate(payload));
    }
    return of(
              new ExceptionApiValidationResult(new RuntimeException(format("Validator not found for %s", mimeType))));
  }

  private AMFShapePayloadValidator getPayloadValidator(String mediaType) {
    return amfConfiguration.withShapePayloadPlugin(new XmlValidationPlugin()).elementClient()
        .payloadValidatorFor(shape, mediaType,
                             ValidationMode.StrictValidationMode());
  }

  private static List<ApiValidationResult> mapToValidationResult(AMFValidationReport validationReport) {
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
