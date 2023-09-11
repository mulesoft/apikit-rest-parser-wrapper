/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import org.junit.Before;
import org.junit.Test;
import org.mule.amf.impl.AMFParser;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.QueryString;
import org.mule.apikit.model.api.ApiReference;

import java.util.Collection;
import java.util.HashMap;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class QueryStringImplTest {

  private static final String GET_ACTION = "GET";
  private QueryString locationsQueryString;
  private QueryString emailQueryString;
  private QueryString historySinceQueryString;
  private QueryString petsQueryString;

  @Before
  public void setUp() throws Exception {
    String apiLocation = this.getClass().getResource("../10-query-string/api.raml").toURI().toString();
    ApiReference apiRef = ApiReference.create(apiLocation);
    ApiSpecification api = new AMFParser(apiRef).parse();
    ResourceImpl resource = (ResourceImpl) api.getResource("/locations");
    ActionImpl action = (ActionImpl) resource.getAction(GET_ACTION);
    locationsQueryString = action.queryString();
    ResourceImpl emails = (ResourceImpl) api.getResources().get("/emails");
    action = (ActionImpl) emails.getResources().get("/details").getAction(GET_ACTION);
    emailQueryString = action.queryString();
    action = (ActionImpl) emails.getResources().get("/historySince").getAction(GET_ACTION);
    historySinceQueryString = action.queryString();
    action = (ActionImpl) emails.getResources().get("/pets").getAction(GET_ACTION);
    petsQueryString = action.queryString();
  }

  @Test
  public void getDefaultValueTest() {
    assertNull(locationsQueryString.getDefaultValue());
  }

  @Test
  public void isArrayTest() {
    assertFalse(locationsQueryString.isArray());
  }

  @Test
  public void validateTest() {
    assertTrue(locationsQueryString.validate(new HashMap<String, Collection<?>>() {

      {
        put("start", singletonList("2"));
        put("lat", singletonList("12"));
        put("long", singletonList("13"));
      }
    }));

    assertTrue(locationsQueryString.validate(new HashMap<String, Collection<?>>() {

      {
        put("start", singletonList("2"));
        put("page-size", singletonList("20"));
        put("location", singletonList("1,2"));
      }
    }));

    assertFalse(locationsQueryString.validate(new HashMap<String, Collection<?>>() {

      {
        put("Not valid query string", singletonList("bad"));
      }
    }));

    assertTrue(emailQueryString.validate(new HashMap<String, Collection<?>>() {

      {
        put("subject", singletonList("Email Subject"));
        put("to", asList("John", "Ana"));
        put("body", singletonList("Email body"));
      }
    }));
  }

  @Test
  public void multipleUnionsValidationTest() {
    assertTrue(petsQueryString.validate(new HashMap<String, Collection<?>>() {

      {
        put("wings", singletonList("2"));
        put("name", singletonList("santa"));
      }
    }));

    assertTrue(petsQueryString.validate(new HashMap<String, Collection<?>>() {

      {
        put("color", singletonList("red"));
        put("name", singletonList("santa"));
      }
    }));

    assertFalse(petsQueryString.validate(new HashMap<String, Collection<?>>() {

      {
        put("color", singletonList("blue"));
        put("wings", singletonList("1"));
      }
    }));
  }

  @Test
  public void isScalarTest() {
    assertFalse(locationsQueryString.isScalar());
    assertFalse(emailQueryString.isScalar());
    assertTrue(historySinceQueryString.isScalar());
  }

  @Test
  public void isFacetArrayTest() {
    assertFalse(locationsQueryString.isFacetArray("UnionShape"));
    assertTrue(emailQueryString.isFacetArray("to"));
  }

  @Test
  public void facetsTest() {
    assertEquals(5, locationsQueryString.facets().size());
    assertEquals(3, emailQueryString.facets().size());
  }
}
