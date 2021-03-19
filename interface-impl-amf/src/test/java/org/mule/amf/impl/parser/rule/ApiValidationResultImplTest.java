/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.amf.impl.parser.rule;

import amf.client.validate.ValidationResult;
import amf.core.annotations.LexicalInformation;
import amf.core.parser.Position;
import amf.core.parser.Range;
import amf.core.validation.AMFValidationResult;
import org.junit.Before;
import org.junit.Test;
import org.mule.amf.impl.AMFParser;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.validation.ApiValidationResult;
import scala.Option;

import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ApiValidationResultImplTest {

  private static final String NO_VALID_RESOURCE_PATH = "not-valid-resource";
  private static final String ERROR_MESSAGE = "expected type: Integer, found: Double";
  public static final String CUSTOM_ERROR_MESSAGE = "This is an error message!";
  public static final String CUSTOM_ENCODED_LOCATION = URLEncoder.encode("C://my-directory");
  public static final String CUSTOM_LOCATION = "Here is the error";
  public static final String FULL_CUSTOM_ERROR_MESSAGE = "This is an error message!\n" +
      "  Location: Here is the error\n" +
      "  Position: Line 1,  Column 1";
  public static final String FULL_CUSTOM_ERROR_ENCODED_MESSAGE = "This is an error message!\n" +
      "  Location: C://my-directory\n" +
      "  Position: Line 1,  Column 1";

  private List<ApiValidationResult> invalidApiResults;

  @Before
  public void setUp() throws Exception {
    String apiLocation = this.getClass().getResource("./invalid.raml").toURI().toString();
    AMFParser parser = new AMFParser(ApiReference.create(apiLocation), false);
    invalidApiResults = parser.validate().getResults();
  }

  @Test(expected = RuntimeException.class)
  public void errorOnInvalidResourcePathTest() throws Exception {
    new AMFParser(ApiReference.create(NO_VALID_RESOURCE_PATH), true);
  }

  @Test
  public void getMessageTest() {
    assertTrue(invalidApiResults.get(0).getMessage().contains(ERROR_MESSAGE));
    assertTrue(invalidApiResults.get(1).getMessage().contains(ERROR_MESSAGE));
  }

  @Test
  public void getLineTest() {
    assertEquals(Optional.empty(), invalidApiResults.get(0).getLine());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getPathTest() {
    assertNull(invalidApiResults.get(0).getPath());
  }

  @Test
  public void getSeverityTest() {
    assertEquals("ERROR", invalidApiResults.get(0).getSeverity().name());
  }

  @Test
  public void toStringTest() {
    assertTrue(invalidApiResults.get(0).toString().contains(ERROR_MESSAGE));
    assertTrue(invalidApiResults.get(1).toString().contains(ERROR_MESSAGE));
  }

  @Test
  public void testWithDetails() throws Exception {
    Position start = new Position(1, 1);
    Option<String> location = Option.apply(CUSTOM_LOCATION);
    String message = CUSTOM_ERROR_MESSAGE;
    ValidationResult validationResult = createAMFValidationResult(message, start, location);
    ApiValidationResultImpl apiValidationResult = new ApiValidationResultImpl(validationResult);

    String actualMessage = apiValidationResult.getMessage();
    String expectedMessage = FULL_CUSTOM_ERROR_MESSAGE;
    assertEquals(actualMessage, expectedMessage);
  }

  @Test
  public void testPartiallyDetails() throws Exception {
    Position start = new Position(0, 0);
    Option<String> location = Option.apply(CUSTOM_LOCATION);
    String message = CUSTOM_ERROR_MESSAGE;
    ValidationResult validationResult = createAMFValidationResult(message, start, location);
    ApiValidationResultImpl apiValidationResult = new ApiValidationResultImpl(validationResult);
    String actualMessage = apiValidationResult.getMessage();
    String expectedMessage = CUSTOM_ERROR_MESSAGE;
    assertEquals(actualMessage, expectedMessage);
  }

  @Test
  public void testEncodedMessage() throws Exception {
    Position start = new Position(1, 1);
    Option<String> location = Option.apply(CUSTOM_ENCODED_LOCATION);
    String message = CUSTOM_ERROR_MESSAGE;
    ValidationResult validationResult = createAMFValidationResult(message, start, location);
    ApiValidationResultImpl apiValidationResult = new ApiValidationResultImpl(validationResult);
    String actualMessage = apiValidationResult.getMessage();
    String expectedMessage = FULL_CUSTOM_ERROR_ENCODED_MESSAGE;
    assertEquals(actualMessage, expectedMessage);
  }

  @Test
  public void testWithoutDetails() throws Exception {
    Position start = new Position(0, 0);
    Option<String> location = Option.empty();
    String message = CUSTOM_ERROR_MESSAGE;
    ValidationResult validationResult = createAMFValidationResult(message, start, location);
    ApiValidationResultImpl apiValidationResult = new ApiValidationResultImpl(validationResult);
    String actualMessage = apiValidationResult.getMessage();
    String expectedMessage = CUSTOM_ERROR_MESSAGE;
    assertEquals(actualMessage, expectedMessage);
  }

  public ValidationResult createAMFValidationResult(String message, Position startPosition, Option<String> location) {
    Option<LexicalInformation> position = Option.apply(new LexicalInformation(new Range(startPosition, new Position(0, 0))));
    AMFValidationResult amfValidationResult =
        new AMFValidationResult(message, "level", "targetNode", Option.empty(), "validationId", position, location, "");
    return new ValidationResult(amfValidationResult);
  }

}
