package ai.concerto.event.controller;


import ai.concerto.event.enums.Channel;
import ai.concerto.event.exchange.GoogleBusinessMessageRequest;
import ai.concerto.event.publisher.UserRequestStreamPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/event")
public class GoogleBusinessMessageController {
  public static final String RESPONSE_MESSAGE = "message-published";
  public static final String PROJECT_ID = "project_id";
  @Autowired
  private UserRequestStreamPublisher userRequestStreamPublisher;
  @Autowired
  private ObjectMapper objectMapper;

  @PostMapping(value = {"/google/business_message/{applicationId}",
      "/{applicationId}/business_message/google"})
  public ResponseEntity<String> processGoogleRequest(@PathVariable String applicationId,
      @RequestBody GoogleBusinessMessageRequest request) throws JsonProcessingException {
    MDC.put(PROJECT_ID, applicationId);
    // verify
    if (!ObjectUtils.isEmpty(request.getClientToken())) {
      return (ResponseEntity.ok(request.getSecret()));
    }
    if (!ObjectUtils.isEmpty(request.getMessage())
        || !ObjectUtils.isEmpty(request.getSuggestionResponse())) {
      userRequestStreamPublisher.publishUserRequest(applicationId,
          objectMapper.writeValueAsString(request), Channel.GOOGLE_BUSINESS_MESSAGE, null);
    }
    return ResponseEntity.ok().build();
  }
}
