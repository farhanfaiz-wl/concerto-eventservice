package ai.concerto.event.service;

import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.notification.handler.DEBotResponseBuilder;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DEService {

  private static final String X_PROJECT_ID = "X-Project-Id";

  @Autowired
  private ObjectMapper snakeCaseMapper;

  @Autowired
  private ServiceDetails serviceDetails;

  @Autowired
  private RestUtils restUtils;


  public DEBotResponse postDeRequest(DERequest deRequest) throws Exception {
    String request;
    try {
      request = snakeCaseMapper.writeValueAsString(deRequest);
      log.debug("Request body to DE {}", deRequest);
    } catch (JsonProcessingException e) {
      log.error("Unable to serialize de request: {}", deRequest.toString(), e);
      throw e;
    }

    String uri = serviceDetails.getDe().getUri() + "/event";

    Properties headerProperties = new Properties();
    headerProperties.put(X_PROJECT_ID, deRequest.getProjectId());

    return convertToDEBotResponse(
        restUtils.postRequest(uri, request, headerProperties, String.class));
  }

  private DEBotResponse convertToDEBotResponse(String strResponse) throws JsonProcessingException {
    Map<String, Object> responseMap = null;
    try {
      log.debug("DE actual response: {}", strResponse);
      responseMap =
          snakeCaseMapper.readValue(strResponse, new TypeReference<Map<String, Object>>() {});
    } catch (JsonProcessingException e) {
      log.error("Unable to convert DE response '{}' to JsonNode", strResponse, e);
      throw e;
    }

    DEBotResponseBuilder deBotResponseBuilder = DEBotResponseBuilder.builder(responseMap)
        .setIsDialogExit().setSessionId().setBotHandlerForm().setBotRepeats().setChangeApp()
        .setConfidence().setCtxForm().setDroppedForm().setHitQAPairId().setHitQATag().setHitSample()
        .setTimestampMs().setTurnTimestampMs().setTurnNo().setInForm().setInQuiz().setUnanswered()
        .setInvalidateLink().setPassToLiveAgent().setQaResults().setQuestionForEmail()
        .setMailReply().setSendEmail().setSuggestASR().setUtilityPhrase().setBotReply();

    return deBotResponseBuilder.build();
  }

}
