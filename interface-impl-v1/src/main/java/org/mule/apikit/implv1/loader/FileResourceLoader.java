/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.loader;

import java.io.File;
import java.io.InputStream;

public class FileResourceLoader extends org.raml.parser.loader.FileResourceLoader {
  public FileResourceLoader(File path) {
    super(path);
  }

  @Override
  public InputStream fetchResource(String resourceName) {
    return super.fetchResource(resourceName.replace("%20", " "));
  }
}
