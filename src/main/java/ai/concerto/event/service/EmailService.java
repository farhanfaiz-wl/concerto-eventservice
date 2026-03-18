package ai.concerto.event.service;

import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.enums.Source;
import ai.concerto.event.exchange.BotResponse;
import ai.concerto.event.utils.RestUtils;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

  private static final String X_PROJECT_ID = "X-Project-Id";
  private static final String SEND_EMAIL_PATH = "%s/email/agent";

  @Autowired
  private RestUtils restUtils;

  @Autowired
  private ServiceDetails serviceDetails;

  public Map<String, Object> sendEmail(String applicationId, String payload) throws Exception {
    String requestUri = String.format(SEND_EMAIL_PATH, serviceDetails.getEmailClient().getUri());
    Properties headerProperties = new Properties();
    headerProperties.put("Authorization", serviceDetails.getEmailClient().getAppKey());
    headerProperties.put(X_PROJECT_ID, applicationId);

    return restUtils.postRequest(requestUri, payload, headerProperties, Map.class);
  }


  public BotResponse getBotResponse(String applicationId) {
    BotResponse botResponse = new BotResponse();
    botResponse.setProjectId(applicationId);
    botResponse.setSource(Source.email.name());
    return botResponse;
  }
}
