/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

/**
 * Represents an error occurred when parsing the input files
 */
public interface ParsingError {

  /**
   * Detailed cause of the error
   *
   * @return a details cause of the error.
   */
  String cause();

}
