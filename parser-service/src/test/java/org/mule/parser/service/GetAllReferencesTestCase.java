package org.mule.parser.service;

import static java.util.Arrays.asList;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mule.parser.service.ParserMode.AMF;
import static org.mule.parser.service.ParserMode.RAML;

import org.mule.apikit.loader.ApiSyncResourceLoader;
import org.mule.apikit.loader.ResourceLoader;
import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.result.ParseResult;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class GetAllReferencesTestCase {

  private static final String API_FOLDER_NAME = "api-with-references";
  private static final String MAIN_API_FILE_NAME = "api.raml";
  private static final String API_RELATIVE_PATH = API_FOLDER_NAME + "/" + MAIN_API_FILE_NAME;
  private static final String APISYNC_NOTATION = "resource::org.mule.parser:references:1.0.0:raml:zip:";
  private static final String ROOT_APISYNC_RAML = APISYNC_NOTATION + MAIN_API_FILE_NAME;

  @Parameter
  public ParserMode mode;

  @Parameters(name = "Parser = {0}")
  public static Iterable<Object[]> data() {
    return asList(new Object[][] {{AMF}, {RAML}});
  }

  public void getAllReferencesWithRemoteRaml() {

  }

  //  @Test
  //  public void getAllReferencesWithRelativePathRoot() {
  //    assertReferences(ApiReference.create(API_RELATIVE_PATH));
  //  }

  @Test
  public void getAllReferencesWithAPISync() {
    ResourceLoader resourceLoader = new ApiSyncResourceLoader(ROOT_APISYNC_RAML, resourceLoaderMock());
    assertReferences(ApiReference.create(ROOT_APISYNC_RAML, resourceLoader));
  }

  @Test
  public void getAllReferencesWithRamlFromUri() throws URISyntaxException {
    URI uri = Thread.currentThread().getContextClassLoader().getResource(API_RELATIVE_PATH).toURI();
    assertReferences(ApiReference.create(uri));
  }

  @Test
  public void getAllReferencesWithAbsolutePathRoot() {
    String path = Thread.currentThread().getContextClassLoader().getResource(API_RELATIVE_PATH).getFile();
    assertReferences(ApiReference.create(path));
  }

  private void assertReferences(ApiReference api) {
    ParseResult parse = mode.getStrategy().parse(api);
    assertThat(parse.success(), is(true));

    List<String> refs = parse.get().getAllReferences();
    assertThat(refs, hasSize(6));
    assertThat(refs, hasItems("/Users/jdesimoni/Workspace/apikit-projects/apikit-rest-parser-wrapper/parser-service/target/test-classes/api-with-references/partner with spaces.raml",
                              "/Users/jdesimoni/Workspace/apikit-projects/apikit-rest-parser-wrapper/parser-service/target/test-classes/api-with-references/data-type.raml",
                              "/Users/jdesimoni/Workspace/apikit-projects/apikit-rest-parser-wrapper/parser-service/target/test-classes/api-with-references/library.raml",
                              "/Users/jdesimoni/Workspace/apikit-projects/apikit-rest-parser-wrapper/parser-service/target/test-classes/api-with-references/company.raml",
                              "/Users/jdesimoni/Workspace/apikit-projects/apikit-rest-parser-wrapper/parser-service/target/test-classes/api-with-references/address.raml",
                              "/Users/jdesimoni/Workspace/apikit-projects/apikit-rest-parser-wrapper/parser-service/target/test-classes/api-with-references/company-example.json"));
  }

  private ResourceLoader resourceLoaderMock() {
    ClassLoader CLL = Thread.currentThread().getContextClassLoader();
    ResourceLoader resourceLoaderMock = mock(ResourceLoader.class);
    List<String> relativePaths = Arrays.asList(MAIN_API_FILE_NAME,
                                               "company.raml",
                                               "company-example.json",
                                               "data-type.raml",
                                               "library.raml",
                                               "partner with spaces.raml",
                                               "address.raml");
    try {
      for (String relativePath : relativePaths) {
        String apisyncResource = API_FOLDER_NAME + "/" + relativePath;
        String apisyncRelativePath = APISYNC_NOTATION + relativePath;
        doReturn(CLL.getResourceAsStream(apisyncResource)).when(resourceLoaderMock).getResourceAsStream(apisyncRelativePath);
        doReturn(CLL.getResource(apisyncResource).toURI()).when(resourceLoaderMock).getResource(apisyncRelativePath);
      }
      return resourceLoaderMock;
    } catch (Exception e) {
      throw new RuntimeException("Something went wrong in the test: " + e.getMessage(), e);
    }
  }
}
