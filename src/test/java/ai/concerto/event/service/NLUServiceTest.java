package ai.concerto.event.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.config.ServiceDetails.Service;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

class NLUServiceTest {

  @InjectMocks
  @Resource
  NLUService nluService;

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
  void getAutoCompleteResultsRestUtilsExceptionTest() throws Exception {
    Service service = new Service();
    service.setAppKey("someAppKey");
    service.setUri("https://someUri");
    Mockito.doReturn(new Service()).when(serviceDetails).getNlu();
    when(serviceDetails.getNlu()).thenReturn(service);

    Properties headerProperties = new Properties();
    headerProperties.put("Authorization", "someAppKey");
    headerProperties.put("X-Project-Id", "someApplicationId");

    when(restUtils.getRequest(
        "https://someUri/api/projects/someApplicationId/qa/autocomplete?text=someText",
        headerProperties, String.class)).thenThrow(ResponseStatusException.class);
    assertThrows(ResponseStatusException.class, () -> {
      nluService.getAutoCompleteResults("someApplicationId", "someText");
    });
  }

  @Test
  void getAnswersFromNlpExceptionTest() throws Exception {
    Service service = new Service();
    service.setUri("https://someUri");
    service.setAppKey("someAppKey");
    Mockito.doReturn(new Service()).when(serviceDetails).getNlu();
    when(serviceDetails.getNlu()).thenReturn(service);

    Properties headerProperties = new Properties();
    headerProperties.put("Authorization", "someAppKey");
    headerProperties.put("X-Project-Id", "someApplicationId");

    when(restUtils.getRequest(
        "https://someUri/api/projects/someApplicationId/qa/?methodId=someMethodId&text=someText&limit=12&filter_tags=someTag",
        headerProperties, String.class)).thenThrow(ResponseStatusException.class);
    List<String> tagList = new ArrayList<>();
    tagList.add("someTag");
    assertThrows(ResponseStatusException.class, () -> {
      nluService.getAnswersFromNLP("someMethodId", "someApplicationId", "someText", 12, tagList);
    });
  }
}
