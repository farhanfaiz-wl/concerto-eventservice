package ai.concerto.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class KrishnaServiceTest {
  @InjectMocks
  @Resource
  KrishnaService krishnaService;

  @Mock
  private ServiceDetails serviceDetails;

  @Mock
  private RestUtils restUtils;

  @Mock
  private ObjectMapper objectMapper;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void getRecommendationFromKrishnaTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("someUri");
    when(serviceDetails.getKrishna()).thenReturn(service);
    Map<String, Object> datamap = new HashMap<>();
    datamap.put("user_identifier", "userId");
    datamap.put("project_identifier", "projectId");

    Properties headerProperties = new Properties();
    headerProperties.put("X-Project-Id", "projectId");

    when(objectMapper.writeValueAsString(datamap)).thenReturn("request");
    when(restUtils.postRequest("someUri/recommendation", "request", headerProperties, String.class))
        .thenReturn("someResponse");
    assertEquals("someResponse",
        krishnaService.getRecommendationFromKrishna("projectId", "userId"));

  }
}
