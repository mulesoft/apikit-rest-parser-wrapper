/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.amf.impl.AMFParser;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.api.ApiReference;
import org.mule.apikit.model.parameter.FileProperties;
import org.mule.apikit.model.parameter.Parameter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ParameterImplTest {

  private static final String BOOKS_RESOURCE = "/books";
  private static final String ACTION_GET = "GET";
  private static final String ISBN = "0321736079";
  private static final String ISBN_QUERY_PARAM = "isbn";
  private static final String TAGS_QUERY_PARAM = "tags";
  private static final String AUTHOR_QUERY_PARAM = "author";
  private static final String PUBLICATION_YEAR_QUERY_PARAM = "publicationYear";
  private static final String TEST_NULL_RESOURCE = "/testNull";
  private static final String MULTIPART_DOCUMENTS = "/documents";
  private static final String ACTION_POST = "POST";
  private static final String MULTIPART_CONTENT_TYPE = "multipart/form-data";

  private static Map<String, Parameter> queryParams;
  private static Map<String, Parameter> testNullQueryParams;
  private static Map<String, List<Parameter>> formParameters;

  @Parameterized.Parameter
  public ApiVendor apiVendor;

  @Parameterized.Parameter(1)
  public ApiSpecification apiSpecification;

  @Parameterized.Parameters(name = "{0}")
  public static Collection apiSpecifications() throws Exception {
    String apiLocation = ParameterImplTest.class.getResource("../10-query-parameters/api.raml").toURI().toString();
    ApiReference ramlApiRef = ApiReference.create(apiLocation);

    apiLocation = ParameterImplTest.class.getResource("../oas20-query-parameters/api.yaml").toURI().toString();
    ApiReference oas20apiRef = ApiReference.create(apiLocation);

    apiLocation = ParameterImplTest.class.getResource("../oas30-query-parameters/api.yaml").toURI().toString();
    ApiReference oas30apiRef = ApiReference.create(apiLocation);

    return Arrays.asList(new Object[][] {
        {ApiVendor.RAML, new AMFParser(ramlApiRef, true).parse()},
        {ApiVendor.OAS_20, new AMFParser(oas20apiRef, true).parse()},
        {ApiVendor.OAS_30, new AMFParser(oas30apiRef, true).parse()}
    });
  }

  @Before
  public void setUp() {
    ResourceImpl resource = (ResourceImpl) apiSpecification.getResource(BOOKS_RESOURCE);
    queryParams = resource.getAction(ACTION_GET).getQueryParameters();
    testNullQueryParams = apiSpecification.getResource(TEST_NULL_RESOURCE).getAction(ACTION_GET).getQueryParameters();
    formParameters = apiSpecification.getResource(MULTIPART_DOCUMENTS).getAction(ACTION_POST)
        .getBody().get(MULTIPART_CONTENT_TYPE).getFormParameters();
  }

  @Test
  public void nonNullableInteger() {
    final Parameter nonNullableInteger = testNullQueryParams.get("nonNullableInteger");
    if (!ApiVendor.OAS_20.equals(apiVendor)) {
      assertFalse(nonNullableInteger.validate(null));
      assertEquals("expected type: Integer, found: Null", nonNullableInteger.message(null));
    }
    assertTrue(nonNullableInteger.validate("123"));
  }

  @Test
  public void nullableInteger() {
    final Parameter nullableInteger = testNullQueryParams.get("nullableInteger");
    if (!ApiVendor.OAS_20.equals(apiVendor)) {
      assertTrue(nullableInteger.validate(null));
    }
    assertTrue(nullableInteger.validate("123"));
  }

  @Test
  public void nonNullableString() {
    final Parameter nonNullableString = testNullQueryParams.get("nonNullableString");
    if (!ApiVendor.OAS_20.equals(apiVendor)) {
      assertFalse(nonNullableString.validate(null));
      assertEquals("expected type: String, found: Null", nonNullableString.message(null));
    }
    assertTrue(nonNullableString.validate("123"));
  }

  @Test
  public void nullableString() {
    final Parameter nullableString = testNullQueryParams.get("nullableString");
    if (!ApiVendor.OAS_20.equals(apiVendor)) {
      assertTrue(nullableString.validate(null));
    }
    assertTrue(nullableString.validate("123"));

  }

  @Test
  public void nullableArray() {
    final Parameter nullableArray = testNullQueryParams.get("nullableArray");
    if (!ApiVendor.OAS_20.equals(apiVendor)) {
      assertTrue(nullableArray.validate(null));
    }
    assertTrue(nullableArray.validate("- \"Hola\"\n- \"Mundo\""));
  }

  @Test
  public void nonNullableArray() {
    final Parameter nonNullableString = testNullQueryParams.get("nonNullableArray");
    if (!ApiVendor.OAS_20.equals(apiVendor)) {
      assertFalse(nonNullableString.validate(null));
      assertEquals("expected type: JSONArray, found: Null", nonNullableString.message(null));
    }
    assertTrue(nonNullableString.validate("- \"Hola\"\n- \"Mundo\""));
  }

  @Test
  public void validateTest() {
    assertTrue(queryParams.get(AUTHOR_QUERY_PARAM).validate("{ \"name\": \"Jose\", \"lastname\": \"Perez\" }"));
    assertFalse(queryParams.get(PUBLICATION_YEAR_QUERY_PARAM).validate("Not valid value"));
    assertTrue(queryParams.get(ISBN_QUERY_PARAM).validate(ISBN));
    assertFalse(queryParams.get(TAGS_QUERY_PARAM).validate("<tagExample>Not valid value</tagExample>"));
  }

  @Test
  public void validateSpecialCharacters() {
    final Parameter parameter = queryParams.get(ISBN_QUERY_PARAM);
    assertTrue(parameter.validate("\\TestTest1"));
    assertTrue(parameter.validate("Test%3A%20Test\\"));
    assertTrue(parameter.validate("\"foo\" is not \"bar\". specials: \b\r\n\f\t\\/"));
    assertTrue(parameter.validate(";',|[]+@<>{}`!\"%"));
    assertTrue(parameter.validate("A',|[]+@<>{}`!\"%"));
    assertTrue(parameter.validate("1',|[]+@<>{}`!\"%"));
    assertTrue(parameter.validate("',|[]+@<>{}`!\"%"));
    assertTrue(parameter.validate("%',|[]+@<>{}`!\"%"));
    assertTrue(parameter.validate("@',|[]+@<>{}`!\"%"));
    assertTrue(parameter.validate("%25%3B%27%2C%7C%5B%5D%2B%40%3C%3E%7B%7D%60%21%22%25"));
    assertTrue(parameter.validate("\"Test:%20Test\""));
    assertTrue(parameter.validate("Tests:Tests"));
    assertTrue(parameter.validate("Test:%20Test"));
    assertTrue(parameter.validate("Test%3A%20Test"));
  }

  @Test
  public void messageTest() {
    assertEquals("expected type: Number, found: String",
                 queryParams.get(PUBLICATION_YEAR_QUERY_PARAM).message("Not valid value"));
    assertEquals("OK", queryParams.get(ISBN_QUERY_PARAM).message(ISBN));
  }

  @Test
  public void isRequiredTest() {
    assertTrue(queryParams.get(ISBN_QUERY_PARAM).isRequired());
    assertFalse(queryParams.get(AUTHOR_QUERY_PARAM).isRequired());
  }

  @Test
  public void getDefaultValueTest() {
    assertNull(queryParams.get(ISBN_QUERY_PARAM).getDefaultValue());
    assertNotNull(queryParams.get(TAGS_QUERY_PARAM).getDefaultValue());
  }

  @Test
  public void isRepeatTest() {
    assertFalse(queryParams.get(AUTHOR_QUERY_PARAM).isRepeat());
    assertTrue(queryParams.get(TAGS_QUERY_PARAM).isRepeat());

  }

  @Test
  public void isArrayTest() {
    assertFalse(queryParams.get(AUTHOR_QUERY_PARAM).isArray());
    assertTrue(queryParams.get(TAGS_QUERY_PARAM).isArray());
  }

  @Test
  public void getDisplayNameTest() {
    if (ApiVendor.RAML.equals(apiVendor)) {
      assertEquals("Author", queryParams.get(AUTHOR_QUERY_PARAM).getDisplayName());
    }
  }

  @Test
  public void getDescriptionTest() {
    assertEquals("An author's full name", queryParams.get(AUTHOR_QUERY_PARAM).getDescription());
  }

  @Test
  public void getExampleTest() {
    if (!ApiVendor.OAS_20.equals(apiVendor)) {
      assertEquals(ISBN, queryParams.get(ISBN_QUERY_PARAM).getExample());
    }
    assertNull(queryParams.get(PUBLICATION_YEAR_QUERY_PARAM).getExample());
  }

  @Test
  public void getExamplesTest() {
    assertEquals(0, queryParams.get(PUBLICATION_YEAR_QUERY_PARAM).getExamples().size());
    if (!ApiVendor.OAS_20.equals(apiVendor)) {
      assertEquals(2, queryParams.get(AUTHOR_QUERY_PARAM).getExamples().size());
    } else {
      assertEquals(0, queryParams.get(AUTHOR_QUERY_PARAM).getExamples().size());
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getInstanceTest() {
    queryParams.get(AUTHOR_QUERY_PARAM).getInstance();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getMetadataTest() {
    queryParams.get(AUTHOR_QUERY_PARAM).getMetadata();
  }

  @Test
  public void isScalarTest() {
    assertTrue(queryParams.get(ISBN_QUERY_PARAM).isScalar());
    if (!ApiVendor.OAS_20.equals(apiVendor)) {
      assertFalse(queryParams.get(AUTHOR_QUERY_PARAM).isScalar());
    } else {
      assertTrue(queryParams.get(AUTHOR_QUERY_PARAM).isScalar());
    }
  }

  @Test
  public void isFacetArrayTest() {
    assertFalse(queryParams.get(ISBN_QUERY_PARAM).isFacetArray(ISBN_QUERY_PARAM));
    assertFalse(queryParams.get(AUTHOR_QUERY_PARAM).isFacetArray("lastname"));
    if (!ApiVendor.OAS_20.equals(apiVendor)) {
      assertTrue(queryParams.get(AUTHOR_QUERY_PARAM).isFacetArray("addresses"));
    } else {
      assertFalse(queryParams.get(AUTHOR_QUERY_PARAM).isFacetArray("addresses"));
    }
  }

  @Test
  public void surroundWithQuotesIfNeededTest() {
    String value = "*321736079";
    assertEquals("\"" + value + "\"", queryParams.get(ISBN_QUERY_PARAM).surroundWithQuotesIfNeeded(value));
    value = ISBN;
    assertEquals(value, queryParams.get(ISBN_QUERY_PARAM).surroundWithQuotesIfNeeded(value));
    value = "Comedy";
    assertEquals("\"" + value + "\"", queryParams.get(TAGS_QUERY_PARAM).surroundWithQuotesIfNeeded(value));
  }

  @Test
  public void getFileProperties() {
    List<Parameter> parameters = formParameters.get("first");
    FileProperties fileProperties = parameters.get(0).getFileProperties().get();
    if (ApiVendor.RAML.equals(apiVendor)) {
      assertThat(fileProperties.getMinLength(), equalTo(8));
      assertThat(fileProperties.getMaxLength(), equalTo(5000));
    }
    if (!ApiVendor.OAS_20.equals(apiVendor)) {
      assertThat(fileProperties.getFileTypes().contains("image/jpeg"), equalTo(true));
    }
  }

  @Test
  public void fileParameterWithoutValues() {
    List<Parameter> parameters = formParameters.get("second");
    Optional<FileProperties> fileProperties = parameters.get(0).getFileProperties();
    if (fileProperties.isPresent()) {
      FileProperties props = fileProperties.get();
      assertThat(props.getMinLength(), equalTo(0));
      assertThat(props.getMaxLength(), equalTo(0));
      assertThat(props.getFileTypes().isEmpty(), equalTo(true));
    }
  }

  @Test
  public void filePropertiesIsEmptyWhenParameterIsNotFile() {
    assertFalse(queryParams.get(AUTHOR_QUERY_PARAM).getFileProperties().isPresent());
  }

}
