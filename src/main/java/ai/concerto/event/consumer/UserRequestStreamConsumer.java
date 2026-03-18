package ai.concerto.event.consumer;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.dto.UserRequestEvent;
import ai.concerto.event.exchange.BotResponse;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.service.UserRequestStreamService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserRequestStreamConsumer
    implements StreamListener<String, ObjectRecord<String, UserRequestEvent>> {

  @Autowired
  private ObjectMapper snakeCaseMapper;

  @Autowired
  private UserRequestStreamService userRequestStreamService;

  @Autowired
  private RedisTemplate redisTemplate;

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Value("${spring.redis.stream.consumer-group}")
  private String consumerGroup;

  @Override
  public void onMessage(ObjectRecord<String, UserRequestEvent> message) {
    try {
      UserRequestEvent event = message.getValue();
      if (StringUtils.hasText(event.getMetaData())) {
        MDC.setContextMap(snakeCaseMapper.readValue(event.getMetaData(),
            new TypeReference<Map<String, String>>() {}));
      }
      userRequestStreamService.processUserRequest(event.getApplicationId(),
          snakeCaseMapper.readValue(event.getRequest(), Object.class), event.getChannel(),
          event.getVendor());
      redisTemplate.opsForStream().acknowledge(consumerGroup, message);
      redisTemplate.opsForStream().delete(message);
    } catch (Exception e) {
      log.error("Unable to process user event: {}", message.getValue(), e);
      redisTemplate.opsForStream().acknowledge(consumerGroup, message);
      analyticsStreamPublisher.publishErrorLogssAnalytics(getBotResponse(message.getValue()),
          e.getMessage());
    } finally {
      MDC.clear();
    }
  }

  private BotResponse getBotResponse(UserRequestEvent value) {
    BotResponse botResponse = new BotResponse();
    botResponse.setProjectId(value.getApplicationId());
    botResponse.setSource(value.getChannel().getName());
    return botResponse;
  }
}
