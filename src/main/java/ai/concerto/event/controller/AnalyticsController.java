package ai.concerto.event.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ai.concerto.event.service.AnalyticsService;

@RestController
@RequestMapping("/event")
public class AnalyticsController {

  @Autowired
  private AnalyticsService analyticsService;

  @GetMapping(path = "/feedback", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> eventFeedback(@RequestParam("payload") String payload) {

    return ResponseEntity.status(HttpStatus.FOUND)
        .location(analyticsService.addUserFeedback(payload)).build();
  }

  @PostMapping(path = "/email_request/{turnId}")
  public ResponseEntity<Void> postEmailRequest(@PathVariable("turnId") String turnId,
      @RequestBody String request) {

    analyticsService.postEmailRequest(request, turnId);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping(path = "user_feedback/{turnId}")
  public ResponseEntity<Void> postChatBotUserFeedBack(@PathVariable("turnId") String turnId,
      @RequestBody String request) {

    analyticsService.postUserFeedback(turnId, request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

}
