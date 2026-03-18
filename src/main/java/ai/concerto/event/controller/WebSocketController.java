package ai.concerto.event.controller;

import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.service.ChatBotService;
import java.security.Principal;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@Controller
public class WebSocketController {

  private static final String RESPONSE_TIME = "response_time";
  private static final String REQUEST_TIMESTAMP_MS = "request_timestamp_ms";

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Autowired
  private ChatBotService chatBotService;

  @MessageMapping("/chatbot")
  public void processChatbotMessage(Principal principal, @RequestBody String message,
      @Header("simpSessionId") String sessionId) throws Exception {

    log.debug("Received chatbot message from user {}", principal.getName());
    chatBotService.processChatbotMessage(principal, message, sessionId);
    MDC.put(RESPONSE_TIME,
        String.valueOf(System.currentTimeMillis() - Long.valueOf(MDC.get(REQUEST_TIMESTAMP_MS))));
  }

  @MessageMapping("/inbox_v1")
  public void processInboxV1AgentMessages(Principal principal, @RequestBody String message,
      @Header("simpSessionId") String sessionId) {
    log.debug("Received inbox v1 agent message from user {}", principal.getName());
    analyticsStreamPublisher.publishAgentMessage(message);
    log.debug("Inbox v1 agent message published to stream");
    MDC.put(RESPONSE_TIME,
        String.valueOf(System.currentTimeMillis() - Long.valueOf(MDC.get(REQUEST_TIMESTAMP_MS))));
  }

  @MessageMapping("/inbox")
  public void processInboxAgentMessages(Principal principal, @RequestBody String message,
      @Header("simpSessionId") String sessionId) {
    log.debug("Received agent message from user {}", principal.getName());
    analyticsStreamPublisher.publishAgentMessage(message);
    log.debug("Agent message published to stream");
    MDC.put(RESPONSE_TIME,
        String.valueOf(System.currentTimeMillis() - Long.valueOf(MDC.get(REQUEST_TIMESTAMP_MS))));
  }

  @MessageExceptionHandler
  @SendToUser("/queue/errors")
  public String handleException(Throwable exception) {
    return exception.getMessage();
  }

}
