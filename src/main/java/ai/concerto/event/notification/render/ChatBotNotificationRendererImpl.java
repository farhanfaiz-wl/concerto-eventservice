package ai.concerto.event.notification.render;

import ai.concerto.event.dto.Session;
import ai.concerto.event.exchange.BotChatBotResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ChatBotNotificationRendererImpl implements ChatBotNotificationRenderer {

  @Override
  public BotChatBotResponse setTimeOutResponse(String projectId, Session session,
      String defaultSessionTimeoutPrompt) {
    List<String> botReplies = new ArrayList<>();
    BotChatBotResponse response = new BotChatBotResponse();
    response.setProjectId(projectId);
    response.setWebSocketUserName(session.getWebSocketUserName());
    response.setSessionId(session.getSessionId());
    response.setUserId(session.getUserId());
    botReplies.add(defaultSessionTimeoutPrompt);
    response.setBotReplies(botReplies);
    return response;
  }
}
