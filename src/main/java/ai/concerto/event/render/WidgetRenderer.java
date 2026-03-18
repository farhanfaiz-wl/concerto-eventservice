package ai.concerto.event.render;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.exchange.DataSet;
import ai.concerto.event.exchange.ChatBotSearchResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WidgetRenderer {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private RedisTemplate redisTemplate;

  @Value("${spring.redis.stream.analytics.turn_logs.key}")
  private String turnLogsStreamKey;

  public Object setSearchResult(DEBotResponse deResponse) {
    ChatBotSearchResponse widgetReponse = new ChatBotSearchResponse();
    try {
      log.info("DE response for search : {}", objectMapper.writeValueAsString(deResponse));
      if (!ObjectUtils.isEmpty(deResponse)) {
        if (!ObjectUtils.isEmpty(deResponse.getBotReply().getSearch())) {
          widgetReponse.setBotRepliesSearch(deResponse.getBotReply().getSearch());
        }
        if (!ObjectUtils.isEmpty(deResponse.getBotReply().getStructuredSearch())) {
          widgetReponse
              .setBotRepliesStructuredSearch(deResponse.getBotReply().getStructuredSearch());
        }
      }
    } catch (Exception ex) {
      log.error("Error occurred while fetching search result from DGI {}", ex);
    }
    return widgetReponse;

  }

  public void setRecommendationInDataSet(DEBotResponse deResponse, DataSet dataSet) {
    if (!ObjectUtils.isEmpty(deResponse)) {
      List<Map<String, String>> botRepliesRcmds = deResponse.getBotReply().getRecommend();
      if (!ObjectUtils.isEmpty(botRepliesRcmds)) {
        botRepliesRcmds.stream().forEach(rcmd -> rcmd.remove("text"));
        dataSet.getData().put("recommendation", botRepliesRcmds);
      }
    }
  }
}
