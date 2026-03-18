package ai.concerto.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.dto.StructuredSearchRequest;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

class StructuredSearchServiceTest {

  @InjectMocks
  @Resource
  public StructuredSearchService structuredSearchService;

  @Mock
  private ServiceDetails serviceDetails;

  @Mock
  private RestUtils restUtils;

  @Mock
  private ObjectMapper mapper;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void fetchStructuredSearchFromSerenExceptionTest() throws Exception {
    StructuredSearchRequest request = new StructuredSearchRequest();
    request.setProjectId("projectId");
    ServiceDetails service = new ServiceDetails();
    service.getSeren().setUri("https://localhost");
    service.getSeren().setAppKey("appKey");

    Properties headerProperties = new Properties();
    headerProperties.put("X-APIKEY", service.getSeren().getAppKey());
    headerProperties.put("X-Project-Id", "projectId");

    when(serviceDetails.getSeren()).thenReturn(service.getSeren());
    when(mapper.writeValueAsString(request.getParams())).thenReturn("someResponse");
    when(restUtils.postRequest(
        "https://localhost/projects/projectId/search?scroll_ttl=scrollTtl&size=12&text=text",
        mapper.writeValueAsString(request.getParams()), headerProperties, String.class))
            .thenThrow(ResponseStatusException.class);
    assertThrows(ResponseStatusException.class, () -> {
      structuredSearchService.fetchStructuredSearchFromSeren("text", 12, "scrollTtl", request);
    });
  }

  @Test
  void fetchStructuredSearchFromSerenTest() throws Exception {
    StructuredSearchRequest request = new StructuredSearchRequest();
    request.setProjectId("projectId");
    ServiceDetails service = new ServiceDetails();
    service.getSeren().setUri("https://localhost");
    service.getSeren().setAppKey("appKey");

    Properties headerProperties = new Properties();
    headerProperties.put("X-APIKEY", service.getSeren().getAppKey());
    headerProperties.put("X-Project-Id", "projectId");

    when(serviceDetails.getSeren()).thenReturn(service.getSeren());
    when(mapper.writeValueAsString(null)).thenReturn("{ \"demoResponse\" : \"response\"}");
    when(restUtils.postRequest(
        "https://localhost/projects/projectId/search?scroll_ttl=scrollTtl&size=12&text=text",
        mapper.writeValueAsString(request.getParams()), headerProperties, String.class))
            .thenReturn("someString");
    structuredSearchService.fetchStructuredSearchFromSeren("text", 12, "scrollTtl", request);
    assertEquals("someString",
        structuredSearchService.fetchStructuredSearchFromSeren("text", 12, "scrollTtl", request));
  }

}
