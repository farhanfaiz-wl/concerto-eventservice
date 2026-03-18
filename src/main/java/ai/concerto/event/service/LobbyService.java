package ai.concerto.event.service;

import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Slf4j
@Service
public class LobbyService {

  private static final String TWILIO_STATUS_UPDATE_URI =
      "%s/v1/lobby/project/%s/messagelog/twilio/internal";
  private static final String X_API_KEY = "X-API-KEY";

  @Autowired
  private ServiceDetails serviceDetails;

  @Autowired
  private RestUtils restUtils;

  @Autowired
  private ObjectMapper objectMapper;

  public void updateTwilioMessageStatus(String applicationId,
      MultiValueMap<String, String> params) {
    String requestUri =
        String.format(TWILIO_STATUS_UPDATE_URI, serviceDetails.getLobby().getUri(), applicationId);
    Map<String, String> request = params.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));
    Properties headerProperties = new Properties();
    headerProperties.put(X_API_KEY, serviceDetails.getLobby().getAppKey());

    try {
      restUtils.postRequest(requestUri, objectMapper.writeValueAsString(request),
          MediaType.APPLICATION_FORM_URLENCODED, headerProperties, String.class);
    } catch (Exception e) {
      log.error("Unable to update twilio message status for app id: {}", applicationId);
    }
  }
}
