package ai.concerto.event.notification.render;

import ai.concerto.event.dto.Session;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.BotSlackResponse;
import org.springframework.util.StringUtils;

public class SlackNotificationRendererImpl implements SlackNotificationRenderer {
    @Override
    public BotSlackResponse setTimeOutResponse(ApplicationIntegration.SlackIntegration slackIntegration,
        Session session, String defaultSessionTimeoutPrompt) {
        BotSlackResponse slackResponse = new BotSlackResponse();
        slackResponse.setToken(slackIntegration.getBotToken());
        slackResponse.setChannel(session.getSlackChannel());
        slackResponse.setText(defaultSessionTimeoutPrompt);
        if (StringUtils.hasText(slackIntegration.getSessionTimeoutPrompt())) {
            slackResponse.setText(slackIntegration.getSessionTimeoutPrompt());
        }
        return slackResponse;
    }
}
