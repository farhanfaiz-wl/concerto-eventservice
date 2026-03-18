package ai.concerto.event.notification.render;

import ai.concerto.event.dto.Session;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.BotSlackResponse;
import org.springframework.stereotype.Component;

@Component
public interface SlackNotificationRenderer {
    BotSlackResponse setTimeOutResponse(ApplicationIntegration.SlackIntegration slackIntegration,
        Session session,String defaultSessionTimeoutPrompt);
}
