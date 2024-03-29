/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.parser.rule;

import amf.core.client.platform.validation.AMFValidationResult;
import org.mule.apikit.validation.ApiValidationResult;
import org.mule.apikit.validation.Severity;
import org.mulesoft.common.client.lexical.Position;
import org.mulesoft.common.client.lexical.PositionRange;

import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.mule.apikit.validation.Severity.ERROR;

public class ApiValidationResultImpl implements ApiValidationResult {

  private static final String ERROR_FORMAT = "%s\n  Location: %s\n  Position: %s";
  private static final String POSITION_FORMAT = "Line %s,  Column %s";

  private AMFValidationResult validationResult;
  private List<String> severities;

  public ApiValidationResultImpl(AMFValidationResult validationResult) {
    this.validationResult = validationResult;
    severities = stream(Severity.values()).map(Enum::name).collect(toList());
  }

  @Override
  public String getMessage() {
    Optional<String> location = validationResult.location();
    PositionRange positionRange = validationResult.position();
    if (location.isPresent() && !positionRange.start().isZero()) {
      return format(ERROR_FORMAT, validationResult.message(), URLDecoder.decode(location.get()),
                    getPositionMessage(positionRange.start()));
    }
    return validationResult.message();
  }

  @Override
  public Optional<Integer> getLine() {
    return Optional.empty();
  }

  @Override
  public String getPath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Severity getSeverity() {
    return !severities.contains(validationResult.severityLevel().toUpperCase()) ? ERROR
        : Severity.fromString(validationResult.severityLevel());
  }

  private static String getPositionMessage(Position startPosition) {
    return format(POSITION_FORMAT, startPosition.line(), startPosition.column());
  }

  @Override
  public String toString() {
    return getMessage();
  }
}
