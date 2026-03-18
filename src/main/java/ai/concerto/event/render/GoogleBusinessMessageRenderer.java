package ai.concerto.event.render;

import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.exchange.BotGoogleBusinessMessageResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class GoogleBusinessMessageRenderer implements ResponseRenderer {
  @Override
  public Object render(Object userRequest, DERequest deRequest, DEBotResponse deResponse) {
    BotGoogleBusinessMessageResponse response = new BotGoogleBusinessMessageResponse();
    response.setConversationId(deRequest.getUserId());
    StringBuilder textBuilder = new StringBuilder();
    deResponse.getBotReply().getText().stream()
        .forEach(text -> textBuilder.append(text.trim()).append(System.lineSeparator()));
    response.setMessage(textBuilder.toString());

    // set suggestions
    List<Map<String, String>> recommendations = new ArrayList<>();
    if (!CollectionUtils.isEmpty(deResponse.getBotReply().getRecommend())) {
      recommendations.addAll(deResponse.getBotReply().getRecommend());
    }
    if (!CollectionUtils.isEmpty(deResponse.getBotReply().getIntentRecommend())) {
      recommendations.addAll(deResponse.getBotReply().getIntentRecommend());
    }
    if (!CollectionUtils.isEmpty(recommendations)) {
      response.setRecommendations(recommendations);
    }

    return response;
  }
}
