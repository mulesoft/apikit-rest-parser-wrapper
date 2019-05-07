/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.parser.visitor;

import static java.util.stream.Collectors.toList;
import static org.raml.parser.rule.ValidationResult.*;

import java.util.List;

import org.mule.apikit.implv1.parser.rule.ApiValidationResultImpl;
import org.mule.apikit.validation.ApiValidationResult;
import org.mule.apikit.visitor.ApiDocumentBuilder;
import org.mule.apikit.visitor.ApiValidationService;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.visitor.RamlValidationService;

public class RamlValidationServiceImpl implements ApiValidationService {

  private RamlDocumentBuilderImpl ramlDocumentBuilderImpl;
  private List<ApiValidationResult> errors;
  private List<ApiValidationResult> warnings;

  public RamlValidationServiceImpl(ApiDocumentBuilder ramlDocumentBuilder) {
    ramlDocumentBuilderImpl = (RamlDocumentBuilderImpl) ramlDocumentBuilder.getInstance();
  }

  public ApiValidationService validate(String resource) {
    return validate(null, resource);
  }

  public ApiValidationService validate(String resourceContent, String resource) {
    ResourceLoader resourceLoader = ramlDocumentBuilderImpl.getResourceLoader();
    RamlValidationService validationService = RamlValidationService.createDefault(resourceLoader);
    List<org.raml.parser.rule.ValidationResult> results = validationService.validate(resourceContent, resource);
    errors = getLevel(Level.ERROR, results).stream().map(vr -> new ApiValidationResultImpl(vr)).collect(toList());
    warnings = getLevel(Level.WARN, results).stream().map(ApiValidationResultImpl::new).collect(toList());
    return this;
  }

  public List<ApiValidationResult> getErrors() {
    return errors;
  }

  public List<ApiValidationResult> getWarnings() {
    return warnings;
  }
}
