package ai.concerto.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.utils.Constants.HttpConstants;
import ai.concerto.event.utils.Constants.MdcConstants;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.MDC;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.http.*;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

class SereneServiceTest {
  @InjectMocks
  @Resource
  SereneService sereneService;

  @Mock
  private RestUtils restUtils;

  @Mock
  private RestTemplate restTemplate;

  @Spy
  private ObjectMapper snakeMaster;

  @Mock
  private RedisTemplate redisTemplate;

  @Mock
  private ServiceDetails serviceDetails;

  @Mock
  private StreamOperations streamOperations;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Disabled
  @Test
  void fetchSchemaAutoCompleteTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("someUri");
    service.setAppKey("appKey");
    when(serviceDetails.getSeren()).thenReturn(service);
    // sereneService.fetchSchemaAutoComplete("projectId", "schemaId", "query", 1);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (!ObjectUtils.isEmpty(MDC.get(MdcConstants.CORRELATION_ID)))
      headers.set(HttpConstants.HEADER_KEY_CORRELATION_ID, MDC.get(MdcConstants.CORRELATION_ID));
    headers.set("X_APIKEY", serviceDetails.getSeren().getAppKey());
    headers.set("X-Project-Id", "projectId");

    HttpEntity<?> entity = new HttpEntity<>(headers);
    ResponseEntity<String> responseEntity =
        new ResponseEntity<String>("someResponse", HttpStatus.OK);
    when(restTemplate.exchange(
        "someUri/projects/projectId/schemas/schemaId/autocomplete?q=query&limit=1", HttpMethod.POST,
        entity, String.class)).thenReturn(responseEntity);
    assertEquals("someResponse",
        sereneService.fetchSchemaAutoComplete("projectId", "schemaId", "query", 1));
  }

  @Disabled
  @Test
  void fetchUniversalAutoCompleteTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("someUri");
    service.setAppKey("appKey");
    when(serviceDetails.getSeren()).thenReturn(service);
    // sereneService.fetchSchemaAutoComplete("projectId", "schemaId", "query", 1);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (!ObjectUtils.isEmpty(MDC.get(MdcConstants.CORRELATION_ID)))
      headers.set(HttpConstants.HEADER_KEY_CORRELATION_ID, MDC.get(MdcConstants.CORRELATION_ID));
    headers.set("X_APIKEY", serviceDetails.getSeren().getAppKey());
    headers.set("X-Project-Id", "projectId");

    HttpEntity<?> entity = new HttpEntity<>(headers);
    ResponseEntity<String> responseEntity =
        new ResponseEntity<String>("someResponse", HttpStatus.OK);
    when(restTemplate.exchange(
        "someUri/projects/projectId/schemas/schemaId/autocomplete?q=query&limit=1", HttpMethod.POST,
        entity, String.class)).thenReturn(responseEntity);
    assertEquals("someResponse", sereneService.fetchUniversalAutoComplete("projectId", "query", 1));
  }

  @Test
  void getAutoCompleteResultsTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("someUri");
    service.setAppKey("appKey");
    when(serviceDetails.getSeren()).thenReturn(service);
    Properties headerProperties = new Properties();
    headerProperties.put("X-APIKEY", serviceDetails.getSeren().getAppKey());
    headerProperties.put("X-Project-Id", "applicationId");

    when(restUtils.postRequest("someUri/search/applicationId/autocomplete?q=text&limit=5", null,
        headerProperties, String.class)).thenReturn(
            "[{\"_id\":\"someId\",\"_score\":" + 12.2 + ",\"complete\":\"someString\"}]");

    assertEquals(1, sereneService.getAutoCompleteResults("applicationId", "text").size());
  }

}
