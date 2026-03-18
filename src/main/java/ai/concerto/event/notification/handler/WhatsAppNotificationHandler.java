package ai.concerto.event.notification.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ai.concerto.event.dto.Session;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.handler.BotResponseHandler;
import ai.concerto.event.notification.render.WhatsAppNotificationRenderer;
import ai.concerto.event.notification.supplier.WhatsAppNotificationHandlerSupplier;
import ai.concerto.event.notification.supplier.WhatsAppNotificationRendererSupplier;
import ai.concerto.event.service.IntegrationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WhatsAppNotificationHandler implements ChannelNotificationHandler {

  @Autowired
  private IntegrationService integrationService;
  @Autowired
  private WhatsAppNotificationRendererSupplier whatsAppNotificationRendererSupplier;
  @Autowired
  private WhatsAppNotificationHandlerSupplier whatsAppNotificationHandlerSupplier;

  @Override
  public void handleChannelResponse(String applicationId, Channel channel, Session session,
      String defaultSessionTimeoutPrompt, ApplicationIntegration integration) {
    ApplicationIntegration.WhatsappIntegration whatsappIntegration =
        (ApplicationIntegration.WhatsappIntegration) integrationService
            .getChannelIntegration(applicationId, channel, integration);
    ApplicationIntegration.ServiceProvider serviceProvider =
        whatsappIntegration.getServiceProvidersList().get(0);
    Vendor vendor =
        whatsappIntegration.getServiceProvidersList().get(0).getMessageServiceProvider();
    try {
      WhatsAppNotificationRenderer whatsAppNotificationRenderer =
          whatsAppNotificationRendererSupplier.getWhatsAppTimeOutRenderer(vendor);
      BotResponseHandler handler =
          whatsAppNotificationHandlerSupplier.getWhatsAppBotResponseHandler(vendor);

      handler.postBotResponse(whatsAppNotificationRenderer.setTimeOutResponse(serviceProvider,
          session, defaultSessionTimeoutPrompt, whatsappIntegration));
      log.info("successfully sent whatsApp timeout notification");
    } catch (Exception e) {
      log.error("error occurred while handling whatsApp timeout notifications: {}", e.getMessage());
    }

  }
}
