/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.parser;

import static org.mule.apikit.common.ApiSyncUtils.isSyncProtocol;

import org.mule.apikit.common.ApiSyncUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
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

  private final ResourceLoader resourceLoader;

  public Raml08ReferenceFinder(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public List<String> detectIncludes(URI ramlURI) throws IOException {
    try {
      String ramlPath = isSyncProtocol(ramlURI.toString()) ? ramlURI.toString().replace(" ", "%20") : ramlURI.getPath();
      String content = IOUtils.toString(resourceLoader.fetchResource(ramlPath), "UTF-8");
      String rootFilePath = getRootFilePath(ramlPath);
      Node rootNode = YAML_PARSER.compose(new StringReader(content));
      if (rootNode == null) {
        return Collections.emptyList();
      } else {
        return new ArrayList<>(includedFilesIn(rootFilePath, rootNode));
      }
    } catch (final MarkedYAMLException e) {
      return Collections.emptyList();
    }
  }

  private String getRootFilePath(String ramlPath) {
    if(ApiSyncUtils.isSyncProtocol(ramlPath)){
      return "";
    }
    return Paths.get(ramlPath).getParent().toString();
  }

  private Set<String> includedFilesIn(String rootFilePath, Node rootNode) throws IOException {
    final Set<String> includedFiles = new HashSet<>();
    if (rootNode.getNodeId() == NodeId.scalar) {
      ScalarNode includedNode = (ScalarNode) rootNode;
      Tag nodeTag = includedNode.getTag();
      if (nodeTag != null && nodeTag.toString().equals(INCLUDE_KEYWORD)) {
        String includedNodeValue = includedNode.getValue();
        String includeUriAsString = includedNodeValue.startsWith("/") ? rootFilePath + includedNodeValue : Paths.get(rootFilePath, includedNodeValue).toString();
        URI includeUri = Paths.get(includeUriAsString).toUri();
        if (resourceLoader.fetchResource(includeUriAsString) != null) {
          includedFiles.add(Paths.get(includeUri).toString());
          includedFiles.addAll(detectIncludes(includeUri));
        }
      }
    } else if (rootNode.getNodeId() == NodeId.mapping) {
      final MappingNode mappingNode = (MappingNode) rootNode;
      final List<NodeTuple> children = mappingNode.getValue();
      for (final NodeTuple childNode : children) {
        final Node valueNode = childNode.getValueNode();
        includedFiles.addAll(includedFilesIn(rootFilePath, valueNode));
      }
    } else if (rootNode.getNodeId() == NodeId.sequence) {
      final SequenceNode sequenceNode = (SequenceNode) rootNode;
      final List<Node> children = sequenceNode.getValue();
      for (final Node childNode : children) {
        includedFiles.addAll(includedFilesIn(rootFilePath, childNode));
      }
    }
    return includedFiles;
  }
}
