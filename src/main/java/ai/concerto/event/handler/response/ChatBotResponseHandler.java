package ai.concerto.event.handler.response;

import ai.concerto.event.exchange.BotChatBotResponse;
import ai.concerto.event.exchange.BotResponse;
import ai.concerto.event.exchange.MessageInfo;
import ai.concerto.event.handler.BotResponseHandler;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Slf4j
@Service
public class ChatBotResponseHandler implements BotResponseHandler {

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  @Autowired
  @Qualifier("camelCaseMapper")
  private ObjectMapper camelCaseMapper;

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Override
  public Object postBotResponse(Object response) {
    try {
      BotChatBotResponse chatBotResponse = (BotChatBotResponse) response;
      if (!ObjectUtils.isEmpty(chatBotResponse.getWebSocketUserName())) {
        messagingTemplate.convertAndSendToUser(chatBotResponse.getWebSocketUserName(),
            "/queue/botmessage", camelCaseMapper.writeValueAsString(response));
        addResponseTime();
        log.info("SuccessFully posted the message");
      }
    } catch (Exception e) {
      log.error("Unable to serialize bot response for chatbot", e);
      analyticsStreamPublisher.publishErrorLogssAnalytics((BotResponse) response, e.getMessage());
    } finally {
      MDC.clear();
    }
    return response;
  }

  @Override
  public MessageInfo getMessageInfo(Object response) {
    // TODO Auto-generated method stub
    return new MessageInfo();
  }
}
