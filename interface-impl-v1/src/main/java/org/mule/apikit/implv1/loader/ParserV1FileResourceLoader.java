/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.loader;

import java.io.File;
import java.io.InputStream;

/**
 * This class has been implemented to work around an issue of the deprecated raml parser v1
 * for version 0.8 apis with spaces in the path
 */
public class ParserV1FileResourceLoader extends org.raml.parser.loader.FileResourceLoader {

  public ParserV1FileResourceLoader(File path) {
    super(path);
  }

  @Override
  public InputStream fetchResource(String resourceName) {
    return super.fetchResource(resourceName.replace("%20", " "));
  }
}
