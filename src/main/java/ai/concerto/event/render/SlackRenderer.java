package ai.concerto.event.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.exchange.BotSlackResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SlackRenderer implements ResponseRenderer {

  @Override
  public Object render(Object userRequest, DERequest deRequest, DEBotResponse deResponse) {
    BotSlackResponse response = new BotSlackResponse();
    response.setToken(deRequest.getBotAuthToken());

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
    // add image/video
    if (!ObjectUtils.isEmpty(deResponse.getBotReply().getImage())
        && !deResponse.getBotReply().getImage().isEmpty()) {
      textBuilder.append(deResponse.getBotReply().getImage().get(0).get("link"));
    } else if (!ObjectUtils.isEmpty(deResponse.getBotReply().getVideo())
        && !deResponse.getBotReply().getVideo().isEmpty()) {
      textBuilder.append(deResponse.getBotReply().getVideo().get(0).get("link"));
    }

    response.setText(textBuilder.toString().trim());
    response.setChannel(deRequest.getSlackChannel());

    return response;
  }

}
