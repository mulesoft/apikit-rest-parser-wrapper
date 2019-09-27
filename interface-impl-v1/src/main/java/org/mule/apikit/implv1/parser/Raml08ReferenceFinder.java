package org.mule.apikit.implv1.parser;

import org.mule.apikit.common.ApiSyncUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
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

  public List<String> detectIncludes(URI ramlUri) throws IOException {
    try {
      final String ramlPath = ramlUri.getPath();
      final String content = IOUtils.toString(resourceLoader.fetchResource(ramlPath), "UTF-8");
      final String rootFilePath = getRootFilePath(ramlPath);

      final Node rootNode = YAML_PARSER.compose(new StringReader(content));
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
    return ramlPath.substring(0, ramlPath.lastIndexOf(File.separator));
  }

  private Set<String> includedFilesIn(String rootFileUri, Node rootNode) throws IOException {
    final Set<String> includedFiles = new HashSet<>();
    if (rootNode.getNodeId() == NodeId.scalar) {
      ScalarNode includedNode = (ScalarNode) rootNode;
      Tag nodeTag = includedNode.getTag();
      if (nodeTag != null && nodeTag.toString().equals(INCLUDE_KEYWORD)) {
        String includedNodeValue = includedNode.getValue();
        String includeUriAsString = includedNodeValue.startsWith("/") ? rootFileUri + includedNodeValue : rootFileUri + "/" + includedNodeValue;
        URI includeUri = URI.create(includeUriAsString.replace(" ", "%20"));
        if (resourceLoader.fetchResource(includeUriAsString) != null) {
          includedFiles.add(includeUri.getPath());
          includedFiles.addAll(detectIncludes(includeUri));
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
