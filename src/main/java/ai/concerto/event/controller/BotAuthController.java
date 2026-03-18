package ai.concerto.event.controller;

import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.handler.response.BotCodeAuthResponse;
import ai.concerto.event.handler.response.StatusResponse;
import ai.concerto.event.publisher.AuthCodeStreamPublisher;
import ai.concerto.event.service.FalconService;

@RestController
@RequestMapping("/v0/bot/auth/code")
public class BotAuthController {

  private static final String PROJECT_ID = "project_id";

  @Autowired
  private FalconService falconService;

  @Autowired
  private AuthCodeStreamPublisher authcodeStreamPublisher;

  @Autowired
  private ObjectMapper objectMapper;



  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BotCodeAuthResponse> getAuthCode(
      @RequestParam("project_id") String projectId, @RequestParam("user_id") String userId) {
    MDC.put(PROJECT_ID, projectId);
    return ResponseEntity
        .ok(new BotCodeAuthResponse(falconService.getRandomAuthCode(projectId, userId)));
  }


  @DeleteMapping()
  public ResponseEntity<BotCodeAuthResponse> deleteAuthCode(
      @RequestParam("project_id") String projectId, @RequestParam("user_id") String userId) {
    MDC.put(PROJECT_ID, projectId);
    authcodeStreamPublisher.publishAuthcodeDeleteRequest(projectId, userId);
    return ResponseEntity.ok(new BotCodeAuthResponse(new StatusResponse(HttpStatus.ACCEPTED.value(),
        "queued", "Delete Request is queued for deletion")));
  }

}
