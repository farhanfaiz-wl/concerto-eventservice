package ai.concerto.event.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.exchange.BotWhatsappRouteResponse;
import ai.concerto.event.exchange.BotWhatsappRouteResponse.WhatsappRouteMedia;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WhatsappRouteRenderer implements ResponseRenderer {

  @Autowired
  private ObjectMapper snakeCaseMapper;

  @Override
  public Object render(Object userRequest, DERequest deRequest, DEBotResponse deResponse) {
    BotWhatsappRouteResponse response = new BotWhatsappRouteResponse();
    response.setAuthId(deRequest.getBotAuthId());
    response.setAuthToken(deRequest.getBotAuthToken());
    response.setPhone(deRequest.getUserId());

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

    response.setText(textBuilder.toString());
    if (!ObjectUtils.isEmpty(deResponse.getBotReply().getImage())
        && !deResponse.getBotReply().getImage().isEmpty()) {
      response.setMedia(new WhatsappRouteMedia());
      response.getMedia().setType("image");
      response.getMedia().setUrl(deResponse.getBotReply().getImage().get(0).get("link"));
      response.getMedia().setCaption("");
    } else if (!ObjectUtils.isEmpty(deResponse.getBotReply().getVideo())
        && !deResponse.getBotReply().getVideo().isEmpty()) {
      response.setMedia(new WhatsappRouteMedia());
      response.getMedia().setType("video");
      response.getMedia().setUrl(deResponse.getBotReply().getVideo().get(0).get("link"));
      response.getMedia().setCaption("");
    }

    return response;
  }


}
