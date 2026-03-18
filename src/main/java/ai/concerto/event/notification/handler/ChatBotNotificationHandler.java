package ai.concerto.event.notification.handler;

import ai.concerto.event.dto.Session;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.handler.response.ChatBotResponseHandler;
import ai.concerto.event.notification.render.ChatBotNotificationRendererImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatBotNotificationHandler implements ChannelNotificationHandler {

  @Autowired
  private ChatBotResponseHandler chatBotResponseHandler;

  @Autowired
  private ChatBotNotificationRendererImpl chatBotNotificationRendererImpl;

  @Override
  public void handleChannelResponse(String applicationId, Channel channel, Session session,
      String defaultSessionTimeoutPrompt, ApplicationIntegration integration) {
    try {
      chatBotResponseHandler.postBotResponse(chatBotNotificationRendererImpl
          .setTimeOutResponse(applicationId, session, defaultSessionTimeoutPrompt));
      log.info("successfully sent chatBot timeout notification");
    } catch (Exception e) {
      log.error("error occurred while handling chatBot timeout notifications: {}", e.getMessage());
    }
  }
}
