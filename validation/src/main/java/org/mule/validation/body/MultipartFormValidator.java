/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.body;


import org.mule.validation.body.form.MultipartFormData;
import org.mule.validation.body.form.MultipartFormDataBuilder;
import org.mule.validation.body.form.MultipartFormDataParameter;
import org.mule.validation.exception.InvalidFormParameterException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;

public class MultipartFormValidator implements BodyValidator {

  private final Map<String, List<Parameter>> formParameters;

  public MultipartFormValidator(MimeType mimeType) {
    this.formParameters = mimeType.getFormParameters();
  }

  @Override
  public TypedValue validate(String charset, TypedValue originalPayload) throws InvalidFormParameterException {
    final InputStream inputStream = unwrapCursorStream(originalPayload.getValue());
    final String boundary = getBoundary(originalPayload);
    MultipartFormDataBuilder multipartFormDataBuilder = new MultipartFormDataBuilder(inputStream, boundary);
    Map<String, MultipartFormDataParameter> actualParameters = multipartFormDataBuilder.getFormDataParameters();

    for (String expectedKey : formParameters.keySet()) {
      List<Parameter> params = formParameters.get(expectedKey);
      if (params != null && params.size() == 1) {
        Parameter expected = params.get(0);
        if (actualParameters.containsKey(expectedKey)) {
          MultipartFormDataParameter multipartFormDataParameter = actualParameters.get(expectedKey);
          multipartFormDataParameter.validate(expected);
        } else {
          if (expected.getDefaultValue() != null) {
            multipartFormDataBuilder.addDefault(expectedKey, expected.getDefaultValue());
          } else if (expected.isRequired()) {
            throw new InvalidFormParameterException("Required form parameter " + expectedKey + " not specified");
          }
        }
      }
    }

    return getTypedValue(multipartFormDataBuilder.build());
  }

  private TypedValue getTypedValue(MultipartFormData multipartFormData) throws InvalidFormParameterException {
    InputStream is = multipartFormData.getInputStream();
    final MediaType mediaType = MediaType.parse(multipartFormData.getContentType());
    DataType dataType = DataType.builder(DataType.INPUT_STREAM).mediaType(mediaType).build();
    return new TypedValue(is, dataType, OptionalLong.of(multipartFormData.getLength()));
  }

  private String getBoundary(TypedValue originalPayload) throws InvalidFormParameterException {
    String boundary = originalPayload.getDataType().getMediaType().getParameter("boundary");
    if (boundary == null) {
      throw new InvalidFormParameterException("Required boundary parameter not found");
    }
    return boundary;
  }

  private InputStream unwrapCursorStream(Object object) {
    return object instanceof CursorProvider ? ((CursorStreamProvider) object).openCursor() : ((InputStream) object);
  }

}
