/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v08;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.mule.apikit.implv2.ParserV2Utils;
import org.mule.apikit.model.ApiSpecification;

import org.junit.Test;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;

public class InterfaceV08TestCase {

//  @Test
//  public void check() {
//    ResourceLoader resourceLoader = new DefaultResourceLoader();
//    ApiSpecification raml = ParserV2Utils.build(resourceLoader, "apis/api-simple/08/full-0.8.raml");
//    assertThat(raml.getVersion(), is("1.0"));
//    assertThat(raml.getSchemas().get(0).size(), is(2));
//    assertThat(raml.getSchemas().get(0).get("UserJson"), containsString("\"firstname\":  { \"type\": \"string\" }"));
//  }
}
