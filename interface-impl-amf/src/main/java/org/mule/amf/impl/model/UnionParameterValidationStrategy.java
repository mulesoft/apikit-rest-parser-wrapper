/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.ProfileName;
import amf.client.model.domain.NilShape;
import amf.client.model.domain.ScalarShape;
import amf.client.model.domain.Shape;
import amf.client.model.domain.UnionShape;
import amf.client.validate.PayloadValidator;
import amf.client.validate.ValidationReport;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.amf.impl.util.LazyValue;

import java.util.Collections;

import static org.mule.amf.impl.model.MediaType.APPLICATION_YAML;
import static org.mule.amf.impl.model.ParameterImpl.needQuotes;
import static org.mule.amf.impl.model.ParameterImpl.quote;

public class UnionParameterValidationStrategy implements ParameterValidationStrategy {

  private boolean autoQuoting;
  private UnionShape unionShape;

  private final LazyValue<PayloadValidator> yamlValidator =
      new LazyValue<>(() -> unionShape.payloadValidator(APPLICATION_YAML)
          .orElseThrow(() -> new ParserException(APPLICATION_YAML + " validator not found for shape " + unionShape)));
  private ValidationReport nullValidationReport;

  public UnionParameterValidationStrategy(UnionShape unionShape) {
    this.unionShape = unionShape;
    this.autoQuoting = needsAutoQuoting(unionShape);
    this.nullValidationReport = new ValidationReport(isNullable(unionShape), "", new ProfileName(""), Collections.emptyList());
  }

  private boolean isNullable(UnionShape unionShape) {
    return unionShape.anyOf().stream().anyMatch(shape -> shape instanceof NilShape);
  }

  @Override
  public ValidationReport validate(String value) {
    if (value == null) {
      return this.nullValidationReport;
    }

    PayloadValidator payloadValidator = yamlValidator.get();
    ValidationReport validationReport = payloadValidator.syncValidate(APPLICATION_YAML, value);
    if (!validationReport.conforms() && autoQuoting) {
      validationReport = payloadValidator.syncValidate(APPLICATION_YAML, quote(value));
    }
    return validationReport;
  }

  private static boolean needsAutoQuoting(UnionShape unionShape) {
    for (Shape shape : unionShape.anyOf()) {
      if (shape instanceof ScalarShape) {
        ScalarShape scalarShape = (ScalarShape) shape;
        if (needQuotes(scalarShape.dataType().value())) {
          return true;
        }
      }
    }
    return false;
  }
}
