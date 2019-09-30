/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.parser;

import static java.util.Collections.emptySet;
import static org.mule.apikit.common.ApiSyncUtils.getApi;
import static org.mule.apikit.common.ApiSyncUtils.isSyncProtocol;
import static org.mule.apikit.common.ReferencesUtils.toURI;

import org.mule.apikit.common.ApiSyncUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.mule.apikit.implv1.loader.SnifferResourceLoader;
import org.raml.parser.loader.ResourceLoader;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

public class Raml08ReferenceFinder {

  private static final Yaml YAML_PARSER = new Yaml();
  private static final String INCLUDE_KEYWORD = "!include";

  private final SnifferResourceLoader resourceLoader;
  private final Path ramlParent;
  private final URI ramlURI;

  public Raml08ReferenceFinder(String ramlPath, SnifferResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
    ramlURI = toURI(ramlPath);
    ramlParent = isSyncProtocol(ramlPath) ? Paths.get(getApi(ramlPath)) : Paths.get(ramlURI.getPath()).getParent();
  }

  public List<String> detectIncludes() throws IOException {
    LinkedHashSet<String> references = detectIncludes(ramlURI);
    references.addAll(resourceLoader.getResources());
    return new ArrayList<>(references);
  }

  private LinkedHashSet<String> detectIncludes(URI uri) throws IOException {
    try {
      Boolean isSync = isSyncProtocol(uri.toString());
      String resourceLocation = isSync ? uri.toString().replace("%20", " ") : uri.getPath();
      String rootFilePath = isSync ? getApi(uri.toString()) + URI.create(ApiSyncUtils.getFileName(uri.toString())).resolve(".") : uri.resolve(".").getPath();

      String content = IOUtils.toString(resourceLoader.fetchResource(resourceLocation), "UTF-8");
      Node rootNode = YAML_PARSER.compose(new StringReader(content));
      if (rootNode != null) {
        return new LinkedHashSet<>(includedFilesIn(Paths.get(rootFilePath), rootNode));
      }
    } catch (final MarkedYAMLException e) {
      return new LinkedHashSet<>();
    }
    return new LinkedHashSet<>();
  }

  private Set<String> includedFilesIn(Path rootFileUri, Node rootNode) throws IOException {
    final Set<String> includedFiles = new HashSet<>();
    if (rootNode.getNodeId() == NodeId.scalar) {
      ScalarNode includedNode = (ScalarNode) rootNode;
      Tag nodeTag = includedNode.getTag();
      if (nodeTag != null && nodeTag.toString().equals(INCLUDE_KEYWORD)) {
        String includedNodeValue = includedNode.getValue();
        Path includedNodePath = Paths.get(includedNodeValue);
        Path includedNodeAbsolutePath = includedNodePath.isAbsolute() ?
            Paths.get(ramlParent.toString(), includedNodePath.toString().substring(1)) : rootFileUri.resolve(includedNodePath);
        if (resourceLoader.fetchResource(includedNodeAbsolutePath.toString()) != null) {
          includedFiles.add(isSyncProtocol(includedNodeAbsolutePath.toString()) ? includedNodeAbsolutePath.normalize().toString() : includedNodeAbsolutePath.normalize().toUri().toString());
          includedFiles.addAll(detectIncludes(toURI(includedNodeAbsolutePath.toString())));
        }
      }
    } else if (rootNode.getNodeId() == NodeId.mapping) {
      final MappingNode mappingNode = (MappingNode) rootNode;
      final List<NodeTuple> children = mappingNode.getValue();
      for (final NodeTuple childNode : children) {
        final Node valueNode = childNode.getValueNode();
        includedFiles.addAll(includedFilesIn(rootFileUri, valueNode));
      }
    } else if (rootNode.getNodeId() == NodeId.sequence) {
      final SequenceNode sequenceNode = (SequenceNode) rootNode;
      final List<Node> children = sequenceNode.getValue();
      for (final Node childNode : children) {
        includedFiles.addAll(includedFilesIn(rootFileUri, childNode));
      }
    }
    return includedFiles;
  }
}
