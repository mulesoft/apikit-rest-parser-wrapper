/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v10;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mule.apikit.common.ApiSyncUtils.isSyncProtocol;
import static org.mule.apikit.implv2.utils.ExchangeDependencyUtils.getExchangeModulePath;

import org.mule.apikit.common.ApiSyncUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.internal.impl.RamlBuilder;
import org.raml.v2.internal.impl.commons.nodes.LibraryNodeProvider;
import org.raml.v2.internal.impl.v10.nodes.LibraryLinkNode;
import org.raml.yagi.framework.nodes.Node;
import org.raml.yagi.framework.nodes.snakeyaml.SYIncludeNode;

public class RamlReferenceFinder {

  private final ResourceLoader resourceLoader;

  public RamlReferenceFinder(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public List<String> getReferences(URI ramlURI) throws IOException {
    return findIncludeNodes(isSyncProtocol(ramlURI.toString()) ? "" : getParent(ramlURI), ramlURI);
  }

  private List<String> findIncludeNodes(String rootPath, URI ramlURI) throws IOException {
    InputStream is = resourceLoader.fetchResource(isSyncProtocol(ramlURI.toString()) ? ramlURI.toString() : ramlURI.getPath());

    if (is == null) {
      return emptyList();
    }

    final Node raml = new RamlBuilder().build(IOUtils.toString(is));
    return findIncludeNodes(rootPath, raml);
  }

  private List<String> findIncludeNodes(String rootPath, final Node raml)
    throws IOException {
    final Set<String> includePaths = new HashSet<>();
    findIncludeNodes(rootPath, "", includePaths, singletonList(raml));
    return new ArrayList<>(includePaths);
  }

  private void findIncludeNodes(String rootPath, String pathRelativeToRoot, Set<String> includePaths,
                                       List<Node> currents)
    throws IOException {

    for (final Node current : currents) {
      // search for include in sources of the current node
      Node possibleInclude = current;
      String pathRelativeToRootCurrent = pathRelativeToRoot;
      while (possibleInclude != null) {
        String includePath = null;
        if (possibleInclude instanceof SYIncludeNode) {
          includePath = ((SYIncludeNode) possibleInclude).getIncludePath();
        } else if (possibleInclude instanceof LibraryLinkNode) {
          includePath = ((LibraryLinkNode) possibleInclude).getRefName();
        }

        if (includePath != null) {
          String sanitize = includePath.replace(" ", "%20");
          String includeAbsolutePath = computeIncludePath(rootPath, pathRelativeToRoot, sanitize);
          URI includedFileAsUri = URI.create(includeAbsolutePath).normalize();
          includePaths.add(includedFileAsUri.getPath());
          includePaths.addAll(findIncludeNodes(rootPath, includedFileAsUri));
          pathRelativeToRootCurrent = calculateNextRootRelative(pathRelativeToRootCurrent, sanitize);
        }

        possibleInclude = possibleInclude.getSource();
      }
      findIncludeNodes(rootPath, pathRelativeToRootCurrent, includePaths, getChildren(current));
    }
  }

  private String getParent(URI uri) {
    URI parentUri = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
    String parentUriAsString = parentUri.toString();
    return parentUriAsString.endsWith("/") ? parentUriAsString.substring(0, parentUriAsString.length() - 1) : parentUriAsString;
  }

  private String fixExchangeModulePath(String path) {
    return getExchangeModulePath(path);
  }

  private String calculateNextRootRelative(String pathRelativeToRootCurrent, String includePath) {
    String newRelativeSubPath = getParent(URI.create(includePath));
    newRelativeSubPath = newRelativeSubPath == null ? "" : newRelativeSubPath;
    return pathRelativeToRootCurrent + newRelativeSubPath;
  }

  private List<Node> getChildren(Node node) {
    if (node instanceof LibraryLinkNode) {
      node = ((LibraryLinkNode) node).getRefNode();
    }
    List<Node> result = new ArrayList<>();
    if (node != null) {
      if (node instanceof LibraryNodeProvider) {
        LibraryNodeProvider libraryNodeProvider = (LibraryNodeProvider) node;
        Node libraryNode = libraryNodeProvider.getLibraryNode();
        if (libraryNode != null) {
          result.add(libraryNode);
        }
      }
      result.addAll(node.getChildren());
    }

    return result;
  }

  private String computeIncludePath(final String rootPath, final String pathRelativeToRoot, final String includePath) {
    // according to RAML 1.0 spec: https://github.com/raml-org/raml-spec/blob/master/versions/raml-10/raml-10.md
    // uses File class to normalize the resolved path acording with the OS (every slash in the path must be in the same direction)
    final String absolutePath = isAbsolute(includePath) //
      ? rootPath + includePath
      // relative path: A path that neither begins with a single slash ("/") nor constitutes a URL, and is interpreted relative to the location of the included file.
      : rootPath + (pathRelativeToRoot.isEmpty() ? "" : "/" + pathRelativeToRoot) + "/" + includePath;
    return fixExchangeModulePath(absolutePath);
  }

  private boolean isAbsolute(String includePath) {
    // absolute path: A path that begins with a single slash ("/") and is interpreted relative to the root RAML file location.
    return includePath.startsWith("/");
  }
}
