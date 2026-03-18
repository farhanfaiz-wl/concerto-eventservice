package ai.concerto.event.service;

import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.exchange.DataSet;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Properties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class NLUService {

  @Data
  public static class NLUAutoCompleteResult {
    @JsonProperty("id")
    public String id;
    @JsonProperty("complete")
    public String complete;
  }

  @Autowired
  private ServiceDetails serviceDetails;

  @Autowired
  private RestUtils restUtils;

  @Autowired
  private ObjectMapper objectMapper;

  private static final String X_PROJECT_ID = "X-Project-Id";
  private static final String NLU_AUTOCOMPLETE_URI_FORMAT = "%s/api/projects/%s/qa/autocomplete";
  private static final String NLU_ANSWERS_URI_FORMAT = "%s/api/projects/%s/qa/";

  public List<NLUAutoCompleteResult> getAutoCompleteResults(String applicationId, String text) {

    UriComponentsBuilder requestUriBuilder = UriComponentsBuilder.fromHttpUrl(String
        .format(NLU_AUTOCOMPLETE_URI_FORMAT, serviceDetails.getNlu().getUri(), applicationId));

    if (StringUtils.hasText(text)) {
      requestUriBuilder.queryParam("text", text);
    }

    Properties headerProperties = new Properties();
    headerProperties.put("Authorization", String.format("%s", serviceDetails.getNlu().getAppKey()));
    headerProperties.put(X_PROJECT_ID, applicationId);

    try {
      log.debug("fetching nlu autocomplete Results : {}", requestUriBuilder.build().toUriString());
      String response = restUtils.getRequest(requestUriBuilder.build().toUriString(),
          headerProperties, String.class);
      return objectMapper.readValue(response, new TypeReference<List<NLUAutoCompleteResult>>() {});
    } catch (Exception e) {
      log.error("NLU autocomplete get API failed : {}", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Unable to process " + requestUriBuilder.build().toUriString());
    }
  }


  public List<DataSet> getAnswersFromNLP(String methodId, String applicationId, String text,
      int nLimit, List<String> tags) {

    UriComponentsBuilder requestUriBuilder = UriComponentsBuilder.fromHttpUrl(
        String.format(NLU_ANSWERS_URI_FORMAT, serviceDetails.getNlu().getUri(), applicationId));

    if (StringUtils.hasText(methodId)) {
      requestUriBuilder.queryParam("methodId", methodId);
    }
    if (StringUtils.hasText(text)) {
      requestUriBuilder.queryParam("text", text);
    }
    if (StringUtils.hasText(String.valueOf(nLimit))) {
      requestUriBuilder.queryParam("limit", nLimit);
    }
    if (!ObjectUtils.isEmpty(tags)) {
      requestUriBuilder.queryParam("filter_tags", tags.get(0));
    }

    Properties headerProperties = new Properties();
    headerProperties.put("Authorization", String.format("%s", serviceDetails.getNlu().getAppKey()));
    headerProperties.put(X_PROJECT_ID, applicationId);

    try {
      log.debug("fetching nlu best answers : {}", requestUriBuilder.build().toUriString());
      String response = restUtils.getRequest(requestUriBuilder.build().toUriString(),
          headerProperties, String.class);
      return objectMapper.readValue(response, new TypeReference<List<DataSet>>() {});
    } catch (Exception e) {
      log.error("error occured while fetching best answers from nlu : {}", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Unable to process " + requestUriBuilder.build().toUriString());
    }
  }

}
