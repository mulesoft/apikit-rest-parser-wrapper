/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.loader;

import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.util.regex.Matcher;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.mule.apikit.implv2.utils.ExchangeDependencyUtils.DEPENDENCY_PATH_PATTERN;
import static org.mule.apikit.implv2.utils.ExchangeDependencyUtils.getExchangeModulePath;

public class ExchangeDependencyResourceLoader implements ResourceLoader {
  private final File workingFolder;
  private final ResourceLoader resourceLoader;

  public ExchangeDependencyResourceLoader() {
    this(null);
  }

  public ExchangeDependencyResourceLoader(File workingFolder) {
    this.resourceLoader = new DefaultResourceLoader();
    this.workingFolder = workingFolder;
  }

  @Nullable
  @Override
  public InputStream fetchResource(String path) {
    if (isNullOrEmpty(path)) {
      return null;
    }

    if(workingFolder != null){
      final String resourceName;
      final Matcher matcher = DEPENDENCY_PATH_PATTERN.matcher(path);
      if (matcher.find()) {
        final int dependencyIndex = path.lastIndexOf(matcher.group(0));
          resourceName = dependencyIndex <= 0 ? path : path.substring(dependencyIndex);
      } else {
        resourceName = path;
      }
      return resourceLoader.fetchResource(new File(workingFolder, resourceName).getPath());
    }
    return resourceLoader.fetchResource(getExchangeModulePath(path));
  }
}
