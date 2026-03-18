package ai.concerto.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.config.ServiceDetails.Service;
import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class DEServiceTest {

  private static final String X_PROJECT_ID = "X-Project-Id";

  @InjectMocks
  @Resource
  DEService deService;

  @Spy
  private ObjectMapper snakeCaseMapper;

  @Mock
  private ServiceDetails serviceDetails;

  @Mock
  private RestUtils restUtils;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void postDeRequestWithException() throws Exception {
    DERequest deRequest = new DERequest();
    when(snakeCaseMapper.writeValueAsString(deRequest)).thenThrow(JsonProcessingException.class);
    assertThrows(Exception.class, () -> deService.postDeRequest(deRequest));
  }

  @Test
  void postDeRequest() throws Exception {
    DERequest deRequest = new DERequest();
    deRequest.setProjectId("projectId");
    when(snakeCaseMapper.writeValueAsString(deRequest))
        .thenReturn("{\"complianceType\":\"hipaa\"}");
    Service service = new Service();
    service.setUri("someUri");
    when(serviceDetails.getDe()).thenReturn(service);
    Resources resources = new Resources();

    Properties headerProperties = new Properties();
    headerProperties.put(X_PROJECT_ID, deRequest.getProjectId());

    Mockito.doReturn(resources.responseString()).when(restUtils).postRequest("someUri/event",
        "{\"complianceType\":\"hipaa\"}", headerProperties, String.class);
    assertEquals(deService.postDeRequest(deRequest).getClass(), DEBotResponse.class);

  }

}
