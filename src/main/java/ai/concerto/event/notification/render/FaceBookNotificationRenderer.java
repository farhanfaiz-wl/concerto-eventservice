package ai.concerto.event.notification.render;

import ai.concerto.event.dto.Session;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.BotFacebookResponse;
import org.springframework.stereotype.Component;

@Component
public interface FaceBookNotificationRenderer {
    BotFacebookResponse setTimeOutResponse(String projectId, Session session,String defaultSessionTimeoutPrompt,
        ApplicationIntegration.FacebookIntegration facebookIntegration);
}
