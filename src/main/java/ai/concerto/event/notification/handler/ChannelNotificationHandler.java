package ai.concerto.event.notification.handler;

import ai.concerto.event.dto.Session;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.exchange.ApplicationIntegration;
import org.springframework.stereotype.Component;

@Component
public interface ChannelNotificationHandler {
    void handleChannelResponse(String applicationId,
        Channel channel, Session session, String defaultSessionTimeoutPrompt,
        ApplicationIntegration integration);
}
