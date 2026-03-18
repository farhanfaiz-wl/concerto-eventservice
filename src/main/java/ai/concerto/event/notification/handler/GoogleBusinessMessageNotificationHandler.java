package ai.concerto.event.notification.handler;

import ai.concerto.event.dto.Session;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.BotGoogleBusinessMessageResponse;
import ai.concerto.event.handler.response.GoogleBusinessMessageResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GoogleBusinessMessageNotificationHandler implements ChannelNotificationHandler {
  @Autowired
  private GoogleBusinessMessageResponseHandler googleBusinessMessageResponseHandler;

  @Override
  public void handleChannelResponse(String applicationId, Channel channel, Session session,
      String defaultSessionTimeoutPrompt, ApplicationIntegration integration) {
    try {
      BotGoogleBusinessMessageResponse googleBusinessMessageResponse =
          new BotGoogleBusinessMessageResponse();
      googleBusinessMessageResponse.setConversationId(session.getUserId());
      googleBusinessMessageResponse.setMessage(defaultSessionTimeoutPrompt);
      googleBusinessMessageResponseHandler.postBotResponse(googleBusinessMessageResponse);
      log.info("successfully sent google business message timeout notification");
    } catch (Exception e) {
      log.error("error occurred while handling  google business message timeout notifications: {}",
          e.getMessage());
    }
  }
}
