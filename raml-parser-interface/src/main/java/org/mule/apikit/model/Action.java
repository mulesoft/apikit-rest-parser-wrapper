/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.model;

import org.mule.apikit.model.parameter.Parameter;

import java.util.List;
import java.util.Map;

import static org.apache.commons.collections.MapUtils.isEmpty;

public interface Action {

  ActionType getType();

  Resource getResource();

  Map<String, MimeType> getBody();

  Map<String, List<Parameter>> getBaseUriParameters();

  Map<String, Parameter> getResolvedUriParameters();

  Map<String, Parameter> getQueryParameters();

  boolean hasBody();

  Map<String, Response> getResponses();

  Map<String, Parameter> getHeaders();

  List<SecurityReference> getSecuredBy();

  List<String> getIs();

  void cleanBaseUriParameters();

  void setHeaders(Map<String, Parameter> headers);

  void setQueryParameters(Map<String, Parameter> queryParameters);

  void setBody(Map<String, MimeType> body);

  void addResponse(String key, Response response);

  void addSecurityReference(String securityReferenceName);

  void addIs(String is);

  QueryString queryString();

  default String getSuccessStatusCode() {
    Map<String, Response> responses = getResponses();
    if (isEmpty(responses) || responses.get("default") != null) {
      return "200";
    }
    for (String status : responses.keySet()) {
      if (status.startsWith("2")) {
        return status;
      }
    }
    return "200";
  }
}
