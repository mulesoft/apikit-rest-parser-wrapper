/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

public class CompositeErrorTestCase {

  @Test
  public void compositeErrorWithoutParsingErrorType() {
    List<DefaultParsingError> child = Arrays.asList(new DefaultParsingError("cause1"));
    CompositeParsingError compositeErrorChild = new CompositeParsingError("description", child);
    assertThat(compositeErrorChild.cause(), is("description:\ncause1"));

    List<DefaultParsingError> children =
        Arrays.asList(new DefaultParsingError("cause1"), new DefaultParsingError("cause2"),
                      new DefaultParsingError("cause3"));
    CompositeParsingError compositeParsingErrorChildren =
        new CompositeParsingError("description", children);
    assertThat(compositeParsingErrorChildren.cause(), is("description:\ncause1\ncause2\ncause3"));
  }

  @Test
  public void compositeErrorWithChildrenAndParsingErrorType() {
    List<DefaultParsingError> children =
        Arrays.asList(new DefaultParsingError("cause1"), new DefaultParsingError("cause2"),
                      new DefaultParsingError("cause3"));
    CompositeParsingError compositeError = new CompositeParsingError("description",  children);
    assertThat(compositeError.cause(), is("description:\ncause1\ncause2\ncause3"));
  }

  @Test
  public void simpleTest() {
    assertThat(new DefaultParsingError("cause").cause(), Matchers.is("cause"));
  }
}
