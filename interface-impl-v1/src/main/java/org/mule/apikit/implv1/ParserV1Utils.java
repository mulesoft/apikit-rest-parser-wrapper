/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1;

import org.mule.apikit.implv1.parser.visitor.RamlDocumentBuilderImpl;
import org.mule.apikit.implv1.parser.visitor.RamlValidationServiceImpl;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.validation.ApiValidationResult;
import org.mule.apikit.visitor.ApiDocumentBuilder;
import org.mule.apikit.visitor.ApiValidationService;

import java.util.ArrayList;
import java.util.List;

import org.raml.parser.loader.ResourceLoader;

public class ParserV1Utils {

  public static List<String> validate(ResourceLoader resourceLoader, String rootFileName, String resourceContent) {
    return validate(null, resourceLoader, rootFileName, resourceContent);
  }

  public static List<String> validate(String resourceFolder, String rootFileName, String resourceContent) {
    return validate(resourceFolder, null, rootFileName, resourceContent);
  }

  public static ApiSpecification build(String content, String resourceFolder, String rootFileName) {
    return build(content, resourceFolder, null, rootFileName);
  }

  public static ApiSpecification build(String content, ResourceLoader resourceLoader, String rootFileName) {
    return build(content, null, resourceLoader, rootFileName);
  }

  private static List<String> validate(String resourceFolder, ResourceLoader resourceLoader, String rootFileName,
                                       String resourceContent) {
    ApiDocumentBuilder ramlDocumentBuilder = getIRamlDocumentBuilder(resourceFolder, resourceLoader);

    List<String> errorsList = new ArrayList<>();
    ApiValidationService validationService = new RamlValidationServiceImpl(ramlDocumentBuilder);
    ApiValidationService result = validationService.validate(resourceContent, rootFileName);
    for (ApiValidationResult validationResult : result.getErrors()) {
      errorsList.add(validationResult.getMessage());
    }
    return errorsList;
  }

  public static ApiSpecification build(String content, String resourceFolder, ResourceLoader resourceLoader, String rootFileName) {
    ApiDocumentBuilder ramlDocumentBuilder = getIRamlDocumentBuilder(resourceFolder, resourceLoader);

    return ramlDocumentBuilder.build(content, rootFileName);
  }

  private static ApiDocumentBuilder getIRamlDocumentBuilder(String resourceFolder, ResourceLoader resourceLoader) {
    ApiDocumentBuilder ramlDocumentBuilder;
    if (resourceLoader == null) {
      ramlDocumentBuilder = new RamlDocumentBuilderImpl();
    } else {
      ramlDocumentBuilder = new RamlDocumentBuilderImpl(resourceFolder, resourceLoader);
    }

    if (resourceFolder != null) {
      ramlDocumentBuilder.addPathLookupFirst(resourceFolder);
    }
    return ramlDocumentBuilder;
  }
}
