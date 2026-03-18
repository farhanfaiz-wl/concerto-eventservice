package ai.concerto.event.notification.render;

import ai.concerto.event.dto.Session;
import ai.concerto.event.exchange.BotChatBotResponse;
import org.springframework.stereotype.Component;

@Component
public interface ChatBotNotificationRenderer {
    BotChatBotResponse setTimeOutResponse(String projectId, Session session,String defaultSessionTimeoutPrompt);
}
