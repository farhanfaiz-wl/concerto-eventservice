package ai.concerto.event.notification.render;

import ai.concerto.event.dto.Session;
import ai.concerto.event.exchange.ApplicationIntegration;
import org.springframework.stereotype.Component;

@Component
public interface WhatsAppNotificationRenderer {
    Object setTimeOutResponse(
        ApplicationIntegration.ServiceProvider serviceProvider, Session session, String defaultSessionTimeoutPrompt,
        ApplicationIntegration.WhatsappIntegration whatsappIntegration);
}
