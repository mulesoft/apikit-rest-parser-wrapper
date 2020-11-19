/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.result.internal;

import org.mule.apikit.model.ApiSpecification;
import org.mule.parser.service.result.ParsingIssue;

import java.util.List;

public interface ParseResult {

  ApiSpecification get();

  boolean success();

  List<ParsingIssue> getErrors();

  List<ParsingIssue> getWarnings();

}
