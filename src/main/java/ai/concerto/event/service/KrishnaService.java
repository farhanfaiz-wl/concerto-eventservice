package ai.concerto.event.service;

import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KrishnaService {

  @Autowired
  private ServiceDetails serviceDetails;

  @Autowired
  private RestUtils restUtils;

  @Autowired
  private ObjectMapper objectMapper;

  private static final String KRISHNA_RECOMENDATION_URI_FORMAT = "%s/recommendation";
  private static final String X_PROJECT_ID = "X-Project-Id";
  public static final String EMPTY_STRING = "";

  public String getRecommendationFromKrishna(String projectId, String userId) {
    log.debug("Fetching recommendation from krishna for cqamodelId:{}, userId:{}", projectId,
        userId);
    StringBuilder uriBuf = new StringBuilder(
        String.format(KRISHNA_RECOMENDATION_URI_FORMAT, serviceDetails.getKrishna().getUri()));

    Map<String, Object> datamap = new HashMap<>();
    datamap.put("user_identifier", userId);
    datamap.put("project_identifier", projectId);

    Properties headerProperties = new Properties();
    headerProperties.put(X_PROJECT_ID, projectId);

    try {
      return restUtils.postRequest(uriBuf.toString(), objectMapper.writeValueAsString(datamap),
          headerProperties, String.class);
    } catch (Exception e) {
      log.error("Error occured while fetching recommendation from krishna", e);
    }
    return EMPTY_STRING;
  }

}
