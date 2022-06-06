/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.common;

import org.mule.apikit.model.ApiVendor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import static org.mule.apikit.model.ApiVendor.OAS_20;
import static org.mule.apikit.model.ApiVendor.OAS_30;
import static org.mule.apikit.model.ApiVendor.RAML_08;
import static org.mule.apikit.model.ApiVendor.RAML_10;

public class ApiVendorUtils {

  private static final String OPENAPI_REGEX;
  private static final String SWAGGER_REGEX;
  static {
    // We want to match the something like openapi:3.0.<minor> or swagger:3.0.<minor> (with optional "" around either
    // the spec type or the version).
    //
    // The version declaration might appear in an almost arbitrary place on the file (JSON or YAML). We want to match it
    // nonetheless. The only circumstance where a match should be discarded is if it appears inside a comment. JSON has
    // no syntax for comments, we just have to forbid YAML comments (they start on a # and end on a newline).
    //
    // We process the input line-by-line. It should be enough to assert that no # appears before the version
    // declaration. This is an overly conservative approximation but should work for any non-adversarial input.
    final String notAComment = "[^#]*";
    final String maybeDoubleQuotes = "\"?";
    final String maybeDoubleOrSingleQuotes = "[\"']?";
    final String skipWhitespace = "\\s*";
    final String matchPrefix = notAComment + skipWhitespace + maybeDoubleQuotes + skipWhitespace;
    final String matchInfix =
        skipWhitespace + maybeDoubleQuotes + skipWhitespace + ":" + skipWhitespace + maybeDoubleOrSingleQuotes + skipWhitespace;
    final String matchPostfix = skipWhitespace + maybeDoubleOrSingleQuotes;
    OPENAPI_REGEX = matchPrefix + "openapi" + matchInfix + "3\\.0.\\d+" + matchPostfix;
    SWAGGER_REGEX = matchPrefix + "swagger" + matchInfix + "2\\.0" + matchPostfix;
  }
  private static final String HEADER_RAML_10 = "#%RAML 1.0";
  private static final String HEADER_RAML_08 = "#%RAML 0.8";
  private static final Pattern OPENAPI_PATTERN = Pattern.compile(OPENAPI_REGEX);
  private static final Pattern SWAGGER_PATTERN = Pattern.compile(SWAGGER_REGEX);

  private ApiVendorUtils() {}

  public static ApiVendor deduceApiVendor(final InputStream is) {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
      String inputLine = getFirstLine(in);
      ApiVendor vendor = getRamlVendor(inputLine);
      if (vendor != null)
        return vendor;

      do {
        if (SWAGGER_PATTERN.matcher(inputLine).lookingAt()) {
          return OAS_20;
        }
        if (OPENAPI_PATTERN.matcher(inputLine).lookingAt()) {
          return OAS_30;
        }
      } while ((inputLine = in.readLine()) != null);
    } catch (final IOException ignored) {
    }
    return RAML_10; // default value
  }

  public static ApiVendor getRamlVendor(InputStream inputStream) {
    ApiVendor vendor = null;

    try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
      final String header = getFirstLine(in);
      vendor = getRamlVendor(header);
    } catch (final IOException ignored) {
    }

    return vendor;
  }

  private static ApiVendor getRamlVendor(String header) {
    if (header.toUpperCase().startsWith(HEADER_RAML_08))
      return RAML_08;
    if (header.toUpperCase().startsWith(HEADER_RAML_10))
      return RAML_10;
    return null;
  }

  private static String getFirstLine(BufferedReader in) throws IOException {
    String line;
    while ((line = in.readLine()) != null) {
      if (line.trim().length() > 0)
        return line;
    }
    return "";
  }

}
