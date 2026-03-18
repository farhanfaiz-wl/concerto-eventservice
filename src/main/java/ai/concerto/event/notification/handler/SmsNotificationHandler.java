package ai.concerto.event.notification.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ai.concerto.event.dto.Session;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.handler.BotResponseHandler;
import ai.concerto.event.notification.render.SmsNotificationRenderer;
import ai.concerto.event.notification.supplier.SmsNotificationHandlerSupplier;
import ai.concerto.event.notification.supplier.SmsNotificationRendererSupplier;
import ai.concerto.event.service.IntegrationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SmsNotificationHandler implements ChannelNotificationHandler {
  @Autowired
  private IntegrationService integrationService;
  @Autowired
  private SmsNotificationRendererSupplier smsNotificationRendererSupplier;
  @Autowired
  private SmsNotificationHandlerSupplier smsNotificationHandlerSupplier;

  @Override
  public void handleChannelResponse(String applicationId, Channel channel, Session session,
      String defaultSessionTimeoutPrompt, ApplicationIntegration integration) {
    ApplicationIntegration.SmsIntegration smsIntegration =
        (ApplicationIntegration.SmsIntegration) integrationService
            .getChannelIntegration(applicationId, Channel.SMS, integration);
    Vendor vendor = smsIntegration.getServiceProvidersList().get(0).getMessageServiceProvider();
    try {
      SmsNotificationRenderer botSmsNotificationRenderer =
          smsNotificationRendererSupplier.getSmsTimeOutRenderer(vendor);
      BotResponseHandler handler = smsNotificationHandlerSupplier.getSmsResponseHandler(vendor);
      handler.postBotResponse(botSmsNotificationRenderer.setTimeOutResponse(session,
          defaultSessionTimeoutPrompt, smsIntegration));
      log.info("successfully sent sms timeout notification");
    } catch (Exception e) {
      log.error("error occurred while handling sms timeout notifications: {}", e.getMessage());
    }
  }
}
