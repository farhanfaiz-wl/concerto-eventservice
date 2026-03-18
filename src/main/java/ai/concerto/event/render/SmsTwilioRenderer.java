package ai.concerto.event.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.exchange.BotSmsResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SmsTwilioRenderer implements ResponseRenderer {

  @Override
  public Object render(Object userRequest, DERequest deRequest, DEBotResponse deResponse) {

    BotSmsResponse response = new BotSmsResponse();
    response.setAuthId(deRequest.getBotAuthId());
    response.setAuthToken(deRequest.getBotAuthToken());
    response.setTo(deRequest.getUserId());
    response.setFrom(deRequest.getBotPhoneNumber());

    StringBuilder textBuilder = new StringBuilder();
    deResponse.getBotReply().getText().stream()
        .forEach(text -> textBuilder.append(text.trim()).append(System.lineSeparator()));
    // add recommendation
    List<Map<String, String>> recommendations = new ArrayList<>();
    if (!CollectionUtils.isEmpty(deResponse.getBotReply().getRecommend())) {
      recommendations.addAll(deResponse.getBotReply().getRecommend());
    }
    if (!CollectionUtils.isEmpty(deResponse.getBotReply().getIntentRecommend())) {
      recommendations.addAll(deResponse.getBotReply().getIntentRecommend());
    }
    final List<Map<String, String>> recommend = recommendations;

    if (!CollectionUtils.isEmpty(recommend)) {
      IntStream.rangeClosed(1, recommend.size()).boxed().map(idx -> {
        Map<String, String> recommendMap = recommend.get(idx - 1);
        return String.format("%d. %s%n", idx,
            recommendMap.getOrDefault(deRequest.getSource().name(), recommendMap.get("text")));
      }).forEach(textBuilder::append);
    }

    response.setMessage(textBuilder.toString().trim());
    return response;
  }

}
