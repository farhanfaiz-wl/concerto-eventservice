package ai.concerto.event.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.exchange.BotMessage;

@Service
public class ApiRenderer implements ResponseRenderer {
  @Override
  public Object render(Object userRequest, DERequest deRequest, DEBotResponse deResponse) {

    BotMessage response = new BotMessage();

    StringBuilder textBuilder = new StringBuilder();
    deResponse.getBotReply().getVoice().stream()
        .forEach(text -> textBuilder.append(text.trim()).append(System.lineSeparator()));
    String singleReply = textBuilder.toString();

    // add recommendation
    List<Map<String, String>> recommendations = new ArrayList<>();
    if (!ObjectUtils.isEmpty(deResponse.getBotReply().getRecommend())
        && !deResponse.getBotReply().getRecommend().isEmpty()) {
      recommendations.addAll(deResponse.getBotReply().getRecommend());
    }

    response.setText(singleReply);

    List<String> suggestions = new ArrayList<>();

    recommendations.forEach(map -> suggestions.add(map.get("text")));

    response.setSuggestions(suggestions);

    return response;
  }

}
