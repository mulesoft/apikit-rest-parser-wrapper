/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.mule.apikit.model.Action;
import org.mule.apikit.model.ActionType;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.model.parameter.Parameter;

import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class AbstractTestCase {

  private static final String MISSING_RESOURCE = "Resource '%s' missing in AMF Resources for uri '%s'";
  private static final String MISSING_ACTION = "Action '%s' missing";
  private static final String MISSING_PARAMETER = "Parameter '%s' missing";

  static void assertEqual(final ApiSpecification actual, final ApiSpecification expected) {

    assertThat(actual.getVersion(), is(equalTo(expected.getVersion())));
    assertThat(actual.getBaseUri(), is(equalTo(expected.getBaseUri())));

    assertParametersEqual(actual.getBaseUriParameters(), expected.getBaseUriParameters());

    assertResourcesEqual(actual.getResources(), expected.getResources());
  }

  static void assertResourcesEqual(final Map<String, Resource> actual, final Map<String, Resource> expected) {

    final String actualKeys = mkString(actual.keySet());
    final String expectedKeys = mkString(expected.keySet());
    assertThat("expected: '" + expectedKeys + "' but was '" + actualKeys + "", actual.size(), is(expected.size()));

    actual.forEach((k, resource) -> {
      assertThat(format(MISSING_RESOURCE, k, resource.getUri()), expected.containsKey(k), is(true));
      assertEqual(resource, expected.get(k));
    });
  }

  static String mkString(final Set<String> set) {
    return set.stream().collect(joining(", "));
  }

  static void assertEqual(final Resource actual, final Resource expected) {
    assertThat(actual.getUri(), is(equalTo(expected.getUri())));
    assertThat(actual.getRelativeUri(), is(equalTo(expected.getRelativeUri())));
    assertThat(actual.getParentUri(), is(equalTo(expected.getParentUri())));
    assertThat(actual.getResolvedUri("v10"), is(equalTo(expected.getResolvedUri("v10"))));
    // Different behaviour in Java Parser 08 & 10
    //assertThat(actual.getDisplayName(), is(equalTo(expected.getDisplayName())));
    assertActionsEqual(actual.getActions(), expected.getActions());
    actual.getActions().keySet().forEach(action -> {
      final String actualAction = actual.getAction(action.name()).getType().name();
      final String expectedAction = expected.getAction(action.name()).getType().name();
      assertThat(actualAction, is(equalTo(expectedAction)));

    });
    assertResourcesEqual(actual.getResources(), expected.getResources());
  }

  static void assertActionsEqual(final Map<ActionType, Action> actual, final Map<ActionType, Action> expected) {

    assertThat(actual.size(), is(expected.size()));

    actual.forEach((k, v) -> {
      assertThat(format(MISSING_ACTION, k), expected.containsKey(k), is(true));
      assertEqual(v, expected.get(k));
    });
  }

  static void assertEqual(final Action actual, final Action expected) {
    assertThat(actual.getType(), is(equalTo(expected.getType())));

    assertParametersEqual(actual.getHeaders(), expected.getHeaders());
    assertParametersEqual(actual.getResolvedUriParameters(), expected.getResolvedUriParameters());
  }

  static void assertParametersEqual(final Map<String, Parameter> actual, final Map<String, Parameter> expected) {

    final String actualKeys = mkString(actual.keySet());
    final String expectedKeys = mkString(expected.keySet());
    assertThat("expected: '" + expectedKeys + "' but was '" + actualKeys + "", actual.size(), is(expected.size()));

    actual.forEach((k, v) -> {
      assertThat(format(MISSING_PARAMETER, k), expected.containsKey(k), is(true));
      assertEqual(v, expected.get(k));
    });
  }

  static void assertEqual(final Parameter actual, final Parameter expected) {
    // Different behaviour in Java Parser 08 & 10
    //assertThat(actual.getDisplayName(), is(equalTo(expected.getDisplayName())));

    assertThat(actual.getDefaultValue(), is(equalTo(expected.getDefaultValue())));
    assertThat(actual.isRepeat(), is(expected.isRepeat()));
    assertThat(actual.isArray(), is(expected.isArray()));
    //  boolean validate(String value);
    //  String message(String value);
    // Different behaviour in Java Parser 08 & 10
    //assertThat(actual.getDisplayName(), is(equalTo(expected.getDisplayName())));
    assertThat(actual.getDescription(), is(equalTo(expected.getDescription())));
    assertThat(actual.getExample(), is(equalTo(expected.getExample())));
    assertThat(actual.getExamples().size(), is(expected.getExamples().size()));

    // Different behaviour in Java Parser 08 & 10
    // Object getInstance();
    // MetadataType getMetadata();
  }
}
