/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1.model;

import org.mule.apikit.model.Template;

public class TemplateImpl implements Template {

  org.raml.model.Template template;

  public TemplateImpl(org.raml.model.Template template) {
    this.template = template;
  }

  public void setDisplayName(String s) {
    template.setDisplayName(s);
  }

  public String getDisplayName() {
    return template.getDisplayName();
  }

}
