package ai.concerto.event.controller;

import ai.concerto.event.exchange.MessageInfo;
import ai.concerto.event.exchange.PushMessage;
import ai.concerto.event.service.PushMessageService;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/event/pushmessage")
public class PushMessageController {

  private static final String PROJECT_ID = "project_id";

  @Autowired
  private PushMessageService pushMessageService;

  @PostMapping(value = {"/{applicationId}"}, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<MessageInfo> processPushMessageRequest(@PathVariable String applicationId,
      @RequestBody PushMessage pushMessage) {
    MDC.put(PROJECT_ID, applicationId);
    return ResponseEntity.ok(pushMessageService.pushMessageOnChannel(pushMessage));
  }
}
