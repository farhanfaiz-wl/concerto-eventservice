package ai.concerto.event.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import ai.concerto.event.exchange.ExternalAgentReplyRequest;
import ai.concerto.event.exchange.StatusResponse;
import ai.concerto.event.exchange.TransferControlRequest;
import ai.concerto.event.service.AnalyticsService;

@RestController
@RequestMapping("/inbox/api")
public class InboxController {

  @Autowired
  private AnalyticsService analyticsService;


  @PostMapping("/control/transfer")
  public StatusResponse transferConversationControl(@RequestBody TransferControlRequest request)
      throws Exception {
    return analyticsService.takeConversationControl(request.getApplicationId(),
        request.getChannel(), request.getUserId(), request.getControl(), request.getAgentDetail());
  }

  @PostMapping("/message/reply")
  public StatusResponse sendAgentReply(@RequestBody ExternalAgentReplyRequest replyRequest)
      throws JsonProcessingException {
    return analyticsService.sendAgentMessage(replyRequest);
  }

}
