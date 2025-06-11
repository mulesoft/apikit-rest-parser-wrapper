/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.attributes;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.union;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

import com.google.common.net.MediaType;
import org.mule.validation.exception.InvalidHeaderException;
import org.mule.validation.exception.NotAcceptableException;
import org.mule.validation.helpers.AttributesHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.util.MultiMap;


public class HeadersValidator implements AttributesValidator {

  private final Action action;
  private final boolean headersStrictValidation;
  private final List<String> mimeTypes;

  public HeadersValidator(Action action, boolean headersStrictValidation) {
    this.action = action;
    this.headersStrictValidation = headersStrictValidation;
    this.mimeTypes = getResponseMimeTypes(action);
  }

  public void validate(HttpRequestAttributes attributes) throws InvalidHeaderException, NotAcceptableException {
    analyseRequestHeaders(attributes.getHeaders());
    analyseAcceptHeader(attributes.getHeaders());
  }

  private void analyseRequestHeaders(MultiMap<String, String> incomingHeaders)
      throws InvalidHeaderException {
    if (headersStrictValidation) {
      validateHeadersStrictly(incomingHeaders);
    }

    for (Map.Entry<String, Parameter> entry : action.getHeaders().entrySet()) {
      final String ramlHeader = entry.getKey();
      final Parameter ramlType = entry.getValue();

      if (ramlHeader.contains("{?}")) {
        final String regex = ramlHeader.replace("{?}", ".*");
        for (String incomingHeader : incomingHeaders.keySet()) {
          if (incomingHeader.matches(regex))
            validateHeader(incomingHeaders.getAll(incomingHeader), ramlHeader, ramlType);
        }
      } else {
        final List<String> values = AttributesHelper
            .getParamsIgnoreCase(incomingHeaders, ramlHeader);
        if (values.isEmpty() && ramlType.isRequired()) {
          throw new InvalidHeaderException("\"Required header '" + ramlHeader + "' not specified\"");
        }
        validateHeader(values, ramlHeader, ramlType);
      }
    }
  }

  private void validateHeadersStrictly(Map<String, String> headers) throws InvalidHeaderException {
    //checks that headers are defined in the RAML
    final Set<String> ramlHeaders = action.getHeaders().keySet().stream()
        .map(String::toLowerCase)
        .collect(toSet());

    final Set<String> templateHeaders = ramlHeaders.stream()
        .filter(header -> header.contains("{?}"))
        .map(header -> header.replace("{?}", ".*"))
        .collect(toSet());

    final Set<String> unmatchedHeaders = headers.keySet().stream()
        .filter(header -> templateHeaders.stream().noneMatch(header::matches))
        .collect(toSet());

    final Set<String> standardHeaders = stream(HeaderName.values())
        .map(header -> header.getName().toLowerCase())
        .collect(toSet());

    final Set<String> undefinedHeaders = difference(unmatchedHeaders, union(ramlHeaders, standardHeaders));

    if (!undefinedHeaders.isEmpty()) {
      throw new InvalidHeaderException(format("\"[%s] %s\"", on(", ").join(undefinedHeaders),
                                              "headers are not defined in RAML strict headers validation property is true."));
    }
  }

  private void validateHeader(List<String> values, String name, Parameter type)
      throws InvalidHeaderException {
    if (values.isEmpty())
      return;

    if (values.size() > 1 && !type.isArray() && !type.isRepeat())
      throw new InvalidHeaderException("Header " + name + " is not repeatable");

    // raml 1.0 array validation
    if (type.isArray()) {
      validateType(name, values, type);
    } else {
      // single header or repeat
      validateType(name, values.get(0), type);
    }
  }

  private void validateType(String name, List<String> values, Parameter type) throws InvalidHeaderException {
    final StringBuilder yamlValue = new StringBuilder();
    for (String value : values)
      yamlValue.append("- ").append(value).append("\n");

    validateType(name, yamlValue.toString(), type);
  }

  private void validateType(String name, String value, Parameter type) throws InvalidHeaderException {
    if (!type.validate(value)) {
      throw new InvalidHeaderException(format("\"Invalid value '%s' for header '%s'\"", value, name));
    }
  }

  private void analyseAcceptHeader(MultiMap<String, String> incomingHeaders) throws NotAcceptableException {
    if (action == null || action.getResponses() == null || mimeTypes.isEmpty()) {
      //no response media-types defined, return no org.mule.validation.body
      return;
    }
    MediaType bestMatch = MimeTypeParser.bestMatch(mimeTypes, AttributesHelper.getAcceptedResponseMediaTypes(incomingHeaders));
    if (bestMatch == null) {
      throw new NotAcceptableException("Accept header does not match defined mimeTypes");
    }
  }

  private List<String> getResponseMimeTypes(Action action) {
    String status = getSuccessStatus(action);
    Response response = action.getResponses().get(status);
    if (response != null && response.hasBody()) {
      return new ArrayList<>(response.getBody().keySet());
    }
    return new ArrayList<>();
  }

  protected String getSuccessStatus(Action action) {
    for (String status : action.getResponses().keySet()) {
      if ("default".equalsIgnoreCase(status)) {
        return "200";
      }
      int code = Integer.parseInt(status);
      if (code >= 200 && code < 300) {
        return status;
      }
    }
    //default success status
    return "200";
  }

}
