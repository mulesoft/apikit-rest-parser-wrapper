/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.body.form;

import org.mule.validation.exception.InvalidFormParameterException;
import org.mule.apikit.model.parameter.Parameter;

public interface MultipartFormDataParameter {

  void validate(Parameter parameter) throws InvalidFormParameterException;
}
