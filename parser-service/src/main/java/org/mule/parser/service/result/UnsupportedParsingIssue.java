/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.result;

public class UnsupportedParsingIssue extends DefaultParsingIssue {

  public UnsupportedParsingIssue(String cause) {
    super(cause);
  }

  @Override
  public String cause() {
    return super.cause();
  }
}
