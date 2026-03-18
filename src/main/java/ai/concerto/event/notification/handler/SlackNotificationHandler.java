package ai.concerto.event.notification.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ai.concerto.event.dto.Session;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.handler.response.SlackResponseHandler;
import ai.concerto.event.notification.render.SlackNotificationRenderer;
import ai.concerto.event.notification.render.SlackNotificationRendererImpl;
import ai.concerto.event.service.IntegrationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SlackNotificationHandler implements ChannelNotificationHandler {
  @Autowired
  private IntegrationService integrationService;
  @Autowired
  private SlackResponseHandler slackResponseHandler;

  @Override
  public void handleChannelResponse(String applicationId, Channel channel, Session session,
      String defaultSessionTimeoutPrompt, ApplicationIntegration integration) {
    try {
      ApplicationIntegration.SlackIntegration slackIntegration =
          (ApplicationIntegration.SlackIntegration) integrationService
              .getChannelIntegration(applicationId, Channel.SLACK, integration);
      SlackNotificationRenderer slackNotificationRenderer = new SlackNotificationRendererImpl();
      slackResponseHandler.postBotResponse(slackNotificationRenderer
          .setTimeOutResponse(slackIntegration, session, defaultSessionTimeoutPrompt));
      log.info("successfully sent slack timeout notification");
    } catch (Exception e) {
      log.error("error occurred while handling slack timeout notifications: {}", e.getMessage());
    }
  }
}
