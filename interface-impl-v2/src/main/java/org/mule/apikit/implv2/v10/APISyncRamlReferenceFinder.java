package org.mule.apikit.implv2.v10;

import org.raml.v2.api.loader.ResourceLoader;

public class APISyncRamlReferenceFinder extends RamlReferenceFinder {

  private final String rootRamlFile;

  public APISyncRamlReferenceFinder(ResourceLoader resourceLoader) {
    super(resourceLoader);
    this.rootRamlFile = findRootRamlFile();
  }

  private String findRootRamlFile() {
    return "";
  }
}
