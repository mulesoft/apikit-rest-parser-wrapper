/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.model.api;

import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.ApiFormat;
import org.mule.apikit.model.ApiVendor;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.mule.apikit.common.ApiSyncUtils.isSyncProtocol;
import static org.mule.apikit.common.ApiVendorUtils.deduceApiVendor;
import static org.mule.apikit.common.ApiVendorUtils.getRamlVendor;
import static org.mule.apikit.model.ApiVendor.RAML_10;

public interface ApiReference {

  static ApiReference create(final String location, final ResourceLoader resourceLoader) {
    if (isSyncProtocol(location)) {
      return resourceLoader != null ? new ApiSyncApiRef(location, resourceLoader) : new ApiSyncApiRef(location);
    }

    try {
      URI uri = new URI(location);
      if (uri.isAbsolute()) {
        return new URIApiRef(uri, resourceLoader);
      }
    } catch (URISyntaxException ignored) {
      // go for default
    }
    // File is the default implementation
    return new DefaultApiRef(location, resourceLoader);
  }

  static ApiReference create(final String location) {
    return create(location, null);
  }

  static ApiReference create(final URI uri) {
    return new URIApiRef(uri);
  }

  String getLocation();

  ApiFormat getFormat();

  InputStream resolve();

  Optional<ResourceLoader> getResourceLoader();

  default ApiVendor getVendor() {
    final ApiFormat format = getFormat();

    if (ApiFormat.RAML.equals(format)) {
      final ApiVendor ramlVendor = getRamlVendor(resolve());
      return ramlVendor != null ? ramlVendor : RAML_10;
    }

    return deduceApiVendor(resolve());
  }
}
