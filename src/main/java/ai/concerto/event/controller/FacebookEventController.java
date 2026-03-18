package ai.concerto.event.controller;

import ai.concerto.event.enums.Channel;
import ai.concerto.event.publisher.UserRequestStreamPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/event/facebook")
public class FacebookEventController {

  public static final String PROJECT_ID = "project_id";

  @Autowired
  private UserRequestStreamPublisher userRequestStreamPublisher;

  @Autowired
  private ObjectMapper objectMapper;

  @GetMapping("/{applicationId}")
  public ResponseEntity<String> processFacebookWebhookVerification(
      @RequestParam("hub.mode") String mode, @RequestParam("hub.challenge") String challenge,
      @RequestParam("hub.verify_token") String verifyToken) {

    if (mode.equalsIgnoreCase("subscribe") && verifyToken.equals("C52ZgR7VaQAS")) {

      return (ResponseEntity.ok(challenge));
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

  }

  @PostMapping("/{applicationId}")
  public ResponseEntity<String> processFacebookRequest(@PathVariable String applicationId,
      @RequestBody String request) throws Exception {
    log.info("Received facebook request: {}", request);
    MDC.put(PROJECT_ID, applicationId);
    userRequestStreamPublisher.publishUserRequest(applicationId,
        objectMapper.readTree(request).get("entry").get(0).toString(), Channel.FACEBOOK, null);
    return ResponseEntity.ok().build();
  }

}
