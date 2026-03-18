package ai.concerto.event.service;

import static org.mockito.Mockito.verify;
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

class EmailServiceTest {

  @InjectMocks
  @Resource
  public EmailService emailService;

  @Mock
  private RestUtils restUtils;

  @Mock
  private ObjectMapper mapper;

  @Mock
  private ServiceDetails serviceDetails;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void sendEmailTestPayload() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("someUri");
    service.setAppKey("apiKey");
    when(serviceDetails.getEmailClient()).thenReturn(service);

    Properties headerProperties = new Properties();
    headerProperties.put("Authorization", service.getAppKey());
    headerProperties.put("X-Project-Id", "applicationId");

    when(restUtils.postRequest("someUri/email/agent", "payload", headerProperties, Map.class))
        .thenReturn(new HashMap<>());
    emailService.sendEmail("applicationId", "payload");
    verify(restUtils).postRequest("someUri/email/agent", "payload", headerProperties, Map.class);
  }
}
