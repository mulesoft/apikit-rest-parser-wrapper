/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.body;

import static org.mule.validation.helpers.AttributesHelper.getMediaType;
import static org.mule.apikit.ApiType.AMF;
import static org.mule.apikit.ApiType.RAML;

import com.google.common.collect.ImmutableList;
import org.mule.validation.exception.UnsupportedMediaTypeException;
import java.util.List;
import java.util.Map.Entry;
import org.mule.apikit.ApiType;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.MimeType;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.validation.schemas.SchemasHandler;

public class BodyValidatorFactory {

  private final Action action;
  private SchemasHandler schemasHandler;
  private final ExpressionManager expressionManager;
  private final boolean parserV2;
  private static final String XML = "xml";
  private static final String JSON = "json";
  private static final String MULTIPART_FORM = "multipart/";
  private static final String URLENCODED_FORM = "application/x-www-form-urlencoded";
  private final List<BodyValidatorBuilder> validatorsBuilders;


  public BodyValidatorFactory(Action action, ApiSpecification api,
      ExpressionManager expressionManager) {
    this.action = action;
    this.expressionManager = expressionManager;
    this.parserV2 = isParserV2(api);
    if (!parserV2) {
      schemasHandler = new SchemasHandler(api);
    }
    this.validatorsBuilders = initialiseBuilders();
  }

  public BodyValidator resolveValidator(String requestMimeType)
      throws UnsupportedMediaTypeException {
    BodyValidator bodyValidator;

    for (BodyValidatorBuilder builder : validatorsBuilders) {
      bodyValidator = builder.createValidator(requestMimeType);
      if (bodyValidator != null) {
        return bodyValidator;
      }

    }
    throw new UnsupportedMediaTypeException(String.format("%s not supported", requestMimeType));
  }

  private boolean isParserV2(ApiSpecification apiSpecification) {
    ApiType parser = apiSpecification.getType();
    return parser == AMF || (parser == RAML && ApiVendor.RAML_10 == apiSpecification
        .getApiVendor());
  }

  private MimeType findMimeType(String requestMimeType, Action action)
      throws UnsupportedMediaTypeException {
    Entry<String, MimeType> foundMimeType = action.getBody().entrySet().stream()
        .filter(entry -> getMediaType(entry.getKey()).equals(requestMimeType))
        .findFirst()
        .orElseThrow(UnsupportedMediaTypeException::new);
    return foundMimeType.getValue();
  }

  private List<BodyValidatorBuilder> initialiseBuilders() {

    BodyValidatorBuilder jsonValidatorBuilder = requestMimeType -> {
      if (requestMimeType.contains(JSON)) {
        if (parserV2) {
          MimeType mimeType = findMimeType(requestMimeType, action);
          return new RestSchemaV2Validator(mimeType);
        }
        return new RestJsonSchemaValidator(
            schemasHandler.resolveJsonSchema(action, requestMimeType));
      }
      return null;
    };

    BodyValidatorBuilder xmlValidatorBuilder = requestMimeType -> {
      if (requestMimeType.contains(XML)) {
        if (parserV2) {
          MimeType mimeType = findMimeType(requestMimeType, action);
          return new RestSchemaV2Validator(mimeType);
        }
        return new RestXmlSchemaValidator(schemasHandler.resolveXmlSchema(action, requestMimeType));
      }
      return null;
    };

    BodyValidatorBuilder urlEncodedValidatorBuilder = requestMimeType -> {
      if (requestMimeType.contains(URLENCODED_FORM)) {
        if (parserV2) {
          MimeType mimeType = findMimeType(requestMimeType, action);
          return new UrlencodedFormV2Validator(mimeType, expressionManager);
        }
        return new UrlencodedFormV1Validator(findMimeType(requestMimeType, action),
            expressionManager);
      }
      return null;

    };

    BodyValidatorBuilder multipartValidatorBuilder = requestMimeType -> {
      if (requestMimeType.contains(MULTIPART_FORM)) {
        return new MultipartFormValidator(findMimeType(requestMimeType, action));
      }
      return null;
    };

    return ImmutableList.of(jsonValidatorBuilder,
        xmlValidatorBuilder,
        urlEncodedValidatorBuilder,
        multipartValidatorBuilder);
  }

  private interface BodyValidatorBuilder {

    BodyValidator createValidator(String requestMimeType) throws UnsupportedMediaTypeException;
  }
}
