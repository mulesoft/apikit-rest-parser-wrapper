/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.attributes;

import static java.lang.String.format;

import org.mule.validation.exception.InvalidUriParametersException;
import java.util.Map;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.extension.http.api.HttpRequestAttributes;

public class UriParametersValidator implements AttributesValidator {

  private final Action action;

  public UriParametersValidator(Action action) {
    this.action = action;
  }

  public void validate(HttpRequestAttributes attributes) throws InvalidUriParametersException {
    for (Map.Entry<String, Parameter> entry : action.getResolvedUriParameters().entrySet()) {
      String value = attributes.getUriParams().get(entry.getKey());
      Parameter uriParameter = entry.getValue();

      if (!uriParameter.validate(value)) {
        throw new InvalidUriParametersException(
            format("\"Invalid value '%s' for org.mule.validation.uri parameter %s\"", value, entry.getKey()));
      }
    }
  }

}
