package ai.concerto.event.notification.render;

import ai.concerto.event.dto.Session;
import ai.concerto.event.exchange.ApplicationIntegration;
import org.springframework.stereotype.Component;

@Component
public interface SmsNotificationRenderer {
    Object setTimeOutResponse(Session session, String defaultSessionTimeoutPrompt,
        ApplicationIntegration.SmsIntegration smsIntegration);
}
