/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.strategy;

import java.util.concurrent.ScheduledExecutorService;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.result.ParseResult;

public interface ParsingStrategy {

  ParseResult parse(ApiReference ref);

  void setExecutor(ScheduledExecutorService executor);

}
