package ai.concerto.event.notification.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ai.concerto.event.dto.Session;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.handler.response.FacebookResponseHandler;
import ai.concerto.event.notification.render.FaceBookNotificationRenderer;
import ai.concerto.event.notification.render.FaceBookNotificationRendererImpl;
import ai.concerto.event.service.IntegrationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FacebookNotificationHandler implements ChannelNotificationHandler {
  @Autowired
  private IntegrationService integrationService;
  @Autowired
  private FacebookResponseHandler facebookResponseHandler;

  @Override
  public void handleChannelResponse(String applicationId, Channel channel, Session session,
      String defaultSessionTimeoutPrompt, ApplicationIntegration integration) {
    try {
      ApplicationIntegration.FacebookIntegration facebookIntegration =
          (ApplicationIntegration.FacebookIntegration) integrationService
              .getChannelIntegration(applicationId, Channel.FACEBOOK, integration);

      FaceBookNotificationRenderer faceBookNotificationRenderer =
          new FaceBookNotificationRendererImpl();
      facebookResponseHandler.postBotResponse(faceBookNotificationRenderer.setTimeOutResponse(
          applicationId, session, defaultSessionTimeoutPrompt, facebookIntegration));
      log.info("successfully sent faceBook timeout notification");
    } catch (Exception e) {
      log.error("error occurred while handling faceBook timeout notifications: {}", e.getMessage());
    }
  }
}
