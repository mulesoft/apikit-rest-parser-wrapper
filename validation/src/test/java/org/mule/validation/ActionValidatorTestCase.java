/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;

import org.mule.validation.config.Configuration;
import org.mule.validation.config.ValidateConfig;
import org.mule.validation.exception.BadRequestException;
import org.mule.validation.exception.NotAcceptableException;
import org.mule.validation.exception.ResourceNotFoundException;
import org.mule.validation.exception.UnsupportedMediaTypeException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javafx.util.Pair;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;
import org.mule.apikit.model.Action;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.parser.service.ParserMode;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.el.ExpressionManager;

@RunWith(Parameterized.class)
public class ActionValidatorTestCase {

  @Parameter
  public ParserMode parserMode;

  @Parameters(name = "PARSER = {0}")
  public static Iterable<Object[]> data() {
    return asList(new Object[][]{
        {ParserMode.RAML},
        {ParserMode.AMF},
    });
  }

  @Test
  public void validateUriParams()
      throws ResourceNotFoundException, BadRequestException, NotAcceptableException {

    Pair<ActionValidator, HttpRequestAttributesBuilder> pair = initialise(
        "uri-params/uri-parameters.raml", "GET", "/constrains/4");

    HttpRequestAttributes attributes = pair.getKey().validateAttributes(pair.getValue().build());

    assertThat(attributes.getUriParams().get("id"), equalTo("4"));

  }

  @Test
  public void validateQueryParams()
      throws ResourceNotFoundException, BadRequestException, NotAcceptableException {

    Pair<ActionValidator, HttpRequestAttributesBuilder> pair = initialise(
        "query-params/query-parameters.raml", "GET", "/constrains");

    ActionValidator validator = pair.getKey();

    MultiMap<String, String> queryParams = new MultiMap<>();
    queryParams.put("sized", "aa");
    queryParams.put("optionalBoolean", "true");

    HttpRequestAttributes attributes = validator.validateAttributes(pair.getValue()
        .queryParams(queryParams)
        .queryString("sized=aa&optionalBoolean=true")
        .build());

    assertThat(attributes.getQueryParams().get("sized"), equalTo("aa"));
    assertThat(attributes.getQueryParams().get("optionalBoolean"), equalTo("true"));
    assertThat(attributes.getQueryParams().get("withDefaultValue"), equalTo("defaultValue"));
    assertThat(attributes.getQueryString(),
        equalTo("sized=aa&optionalBoolean=true&withDefaultValue=defaultValue"));

  }

  @Test
  public void validateHeaders()
      throws ResourceNotFoundException, BadRequestException, NotAcceptableException {

    Pair<ActionValidator, HttpRequestAttributesBuilder> pair = initialise(
        "headers/validation10.raml", "GET", "/headers-test");

    MultiMap<String, String> headers = new MultiMap<>();
    headers.put("testing", "3");

    HttpRequestAttributes attributes = pair.getKey().validateAttributes(
        pair.getValue()
            .headers(headers)
            .build());

    assertThat(attributes.getHeaders().get("testing"), equalTo("3"));
    assertThat(attributes.getHeaders().get("defaultHeaderValue"), equalTo("hola-header"));
  }

  @Test
  public void validateQueryString()
      throws ResourceNotFoundException, BadRequestException, NotAcceptableException {
    Pair<ActionValidator, HttpRequestAttributesBuilder> pair = initialise(
        "query-string/query-string.raml", "GET", "/simple-type");

    MultiMap<String, String> queryParams = new MultiMap<>();
    queryParams.put("property", "ARG");

    HttpRequestAttributes attributes = pair.getKey().validateAttributes(
        pair.getValue()
            .queryParams(queryParams)
            .queryString("property=ARG")
            .build());

    assertThat(attributes.getQueryString(), equalTo("property=ARG"));

  }

  @Test
  public void validateJsonBody10()
      throws ResourceNotFoundException, UnsupportedMediaTypeException, BadRequestException {

    Pair<ActionValidator, HttpRequestAttributesBuilder> pair = initialise(
        "body/raml-10-with-schema.raml", "PUT", "/schema10user");

    String requestBody = "{\"username\":\"gbs\",\"firstName\":\"george\",\"lastName\":\"bernard shaw\",\"emailAddresses\":[\"gbs@ie\"]}";

    TypedValue<String> typedValue = new TypedValue<>(requestBody, null);

    TypedValue result = pair.getKey().validateBody("application/json", "UTF-8", typedValue);

    assertThat(result.getValue(), equalTo(requestBody));

  }

  @Test
  public void validateJsonBody08()
      throws ResourceNotFoundException, UnsupportedMediaTypeException, BadRequestException {

    Pair<ActionValidator, HttpRequestAttributesBuilder> pair = initialise(
        "body/raml-08-with-schema.raml", "PUT", "/schema");

    String requestBody = "{\"username\":\"gbs\",\"firstName\":\"george\",\"lastName\":\"bernard shaw\",\"emailAddresses\":[\"gbs@ie\"]}";

    TypedValue<String> typedValue = new TypedValue<>(requestBody, null);

    TypedValue result = pair.getKey().validateBody("application/json", "UTF-8", typedValue);

    assertThat(result.getValue(), equalTo(requestBody));

  }

  @Test
  public void validateXmlBody10()
      throws ResourceNotFoundException, UnsupportedMediaTypeException, BadRequestException {

    Pair<ActionValidator, HttpRequestAttributesBuilder> pair = initialise(
        "body/raml-10-with-schema.raml", "PUT", "/schema10user");

    String requestBody = "<user xmlns=\"http://mulesoft.org/schemas/sample\" username=\"gbs\" firstName=\"george\" lastName=\"bernard shaw\"><email-addresses><email-address>gbs@ie</email-address></email-addresses></user>";

    TypedValue<String> typedValue = new TypedValue<>(requestBody, null);

    TypedValue result = pair.getKey().validateBody("text/xml", "UTF-8", typedValue);

    assertThat(result.getValue(), equalTo(requestBody));
  }

  @Test
  public void validateXmlBody08()
      throws ResourceNotFoundException, UnsupportedMediaTypeException, BadRequestException {

    Pair<ActionValidator, HttpRequestAttributesBuilder> pair = initialise(
        "body/raml-08-with-schema.raml", "PUT", "/schema");

    String requestBody = "<user xmlns=\"http://mulesoft.org/schemas/sample\" username=\"gbs\" firstName=\"george\" lastName=\"bernard shaw\"><email-addresses><email-address>gbs@ie</email-address></email-addresses></user>";

    TypedValue<String> typedValue = new TypedValue<>(requestBody, null);

    TypedValue result = pair.getKey().validateBody("text/xml", "UTF-8", typedValue);

    assertThat(result.getValue(), equalTo(requestBody));

  }

  @Test
  public void validateURLEncoded10()
      throws ResourceNotFoundException, UnsupportedMediaTypeException, BadRequestException {
    testURLEncoded("body/urlencoded-body-api-10.raml");
  }

  @Test
  public void validateURLEncoded08()
      throws ResourceNotFoundException, UnsupportedMediaTypeException, BadRequestException {
    testURLEncoded("body/urlencoded-body-api-08.raml");
  }

  @Test
  public void validateMultipart()
      throws ResourceNotFoundException, UnsupportedMediaTypeException, BadRequestException, IOException {

    Configuration configuration = new ValidateConfig("body/form-body-api-10.raml", false, false, parserMode, null);

    ResourceFinder resourceFinder = new ResourceFinder(configuration.getApiSpecification());

    Pair<Action, Map<String, String>> pair = resourceFinder.findAction("POST", "/multipart");

    ActionValidator actionValidator = new ActionValidator(pair.getKey(), configuration);


    String body = "------=_Part_441_1378742620.1576607102867\r\n"
        + "Content-Type: text/plain\r\n"
        + "Content-Disposition: form-data; name=\"first\"\r\n"
        + "\r\n"
        + "primero\r\n"
        + "------=_Part_441_1378742620.1576607102867\r\n"
        + "Content-Type: text/plain\r\n"
        + "Content-Disposition: form-data; name=\"payload\"\r\n"
        + "\r\n"
        + "3.4\r\n"
        + "------=_Part_441_1378742620.1576607102867--\n";

    DataType dataType = DataType
        .builder()
        .type(InputStream.class)
        .mediaType(
        "multipart/form-data; boundary=\"----=_Part_441_1378742620.1576607102867\"")
        .charset("UTF-8")
        .build();

    TypedValue<InputStream> typedValue = new TypedValue<>(new ByteArrayInputStream(body.getBytes()), dataType);

    String result = IOUtils.toString((InputStream) actionValidator
        .validateBody("multipart/form-data", "UTF-8", typedValue)
        .getValue());

    assertTrue(result.contains("third"));
    assertTrue(result.contains("second"));
    assertTrue(result.contains("first"));
    assertTrue(result.contains("payload"));


  }

  private Pair<ActionValidator, HttpRequestAttributesBuilder> initialise(String raml, String method,
      String requestPath)
      throws ResourceNotFoundException {

    Configuration configuration = new ValidateConfig(
        raml,
        false, false,
        parserMode, null
    );

    ResourceFinder resourceFinder = new ResourceFinder(configuration.getApiSpecification());

    Pair<Action, Map<String, String>> pair = resourceFinder.findAction(method, requestPath);

    ActionValidator actionValidator = new ActionValidator(pair.getKey(), configuration);

    HttpRequestAttributesBuilder attributesBuilder = getAttributesBuilder(method, requestPath)
        .uriParams(pair.getValue());

    return new Pair<>(actionValidator, attributesBuilder);
  }

  private HttpRequestAttributesBuilder getAttributesBuilder(String method, String path) {
    return new HttpRequestAttributesBuilder()
        .listenerPath("/api/*")
        .method(method)
        .version("1")
        .scheme("http")
        .relativePath(path)
        .requestPath(path)
        .rawRequestPath(path)
        .requestUri("/")
        .rawRequestUri("/")
        .localAddress("")
        .queryString("")
        .remoteAddress("");
  }

  private void testURLEncoded(String api)
      throws ResourceNotFoundException, UnsupportedMediaTypeException, BadRequestException {
    MultiMap<String, String> parameters = new MultiMap<>();
    parameters.put("second", "segundo");
    parameters.put("third", "true");

    ExpressionManager expressionManager = Mockito.mock(ExpressionManager.class);

    Mockito.when(
        expressionManager.evaluate(
            Mockito.anyString(),
            (DataType) anyObject(),
            anyObject()))
        .thenReturn(new TypedValue(parameters, null));

    Mockito.when(
        expressionManager
            .evaluate(Mockito.eq("output application/x-www-form-urlencoded --- payload"),
                (DataType) anyObject(),
                anyObject()))
        .thenReturn(new TypedValue("second=segundo&third=true&first=primo", null));

    Configuration configuration = new ValidateConfig(
        api,
        false, false,
        parserMode, expressionManager
    );

    ResourceFinder resourceFinder = new ResourceFinder(configuration.getApiSpecification());

    Pair<Action, Map<String, String>> pair = resourceFinder
        .findAction("POST", "/url-encoded-with-default");

    ActionValidator actionValidator = new ActionValidator(pair.getKey(), configuration);

    TypedValue<String> typedValue = new TypedValue<>("second=segundo&third=true", null);

    TypedValue result = actionValidator
        .validateBody("application/x-www-form-urlencoded", "UTF-8", typedValue);

    assertThat(result.getValue(), equalTo("second=segundo&third=true&first=primo"));
  }
}
