/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import org.mule.apikit.loader.ResourceLoader;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.String.format;
import static org.mule.apikit.common.ApiSyncUtils.isSyncProtocol;

public class ApiSyncResourceLoader implements ResourceLoader {

  private static final String protocol = "jar:file:";
  private static final String API_SYNC_PROTOCOL = "resource::";
  private final String localRepository =
      new File("").getAbsolutePath().replace("\\", "/") + "/src/test/resources/.m2/repository";

  @Override
  public URI getResource(String resource) {
    try {
      if (!isSyncProtocol(resource)) {
        return null;
      }

      String[] parts = resource.substring(API_SYNC_PROTOCOL.length()).split(":");

      String groupId = parts[0].replaceAll("\\.", "/");
      String artifact = parts[1];
      String version = parts[2];
      String classifier = parts[3];
      String file = parts[5].replaceAll(" ", "%20");

      return new URI(
                     format("%s%s/%s/%s/%s/%s-%s-%s.zip!/%s", protocol, localRepository,
                            groupId, artifact, version, artifact, version, classifier, file));
    } catch (URISyntaxException e) {
      return null;
    }
  }

}
