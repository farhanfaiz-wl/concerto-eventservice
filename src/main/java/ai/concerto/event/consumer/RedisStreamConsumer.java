package ai.concerto.event.consumer;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.exchange.PushMessage;
import ai.concerto.event.handler.event.ControlEventHandler;
import ai.concerto.event.handler.event.LogEventHandler;
import ai.concerto.event.service.PushMessageService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RedisStreamConsumer
    implements StreamListener<String, MapRecord<String, String, String>> {

  private static final String TYPE = "type";
  private static final String EVENT = "event";

  @Autowired
  private LogEventHandler logEventHandler;

  @Autowired
  private ControlEventHandler controlEventHandler;

  @Autowired
  private RedisTemplate redisTemplate;

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private PushMessageService pushMessageService;

  @Value("${spring.redis.stream.consumer-group}")
  private String consumerGroup;

  @Override
  public void onMessage(MapRecord<String, String, String> message) {
    Map<String, String> data = message.getValue();
    log.info("Received stream message: {} with id: {}", data, message.getId());
    /*
     * boolean deleteRecord = true; switch (EventType.valueOf(data.get(TYPE).toUpperCase())) { case
     * BOT_TURN: case USER_MESSAGE: logEventHandler.handleEvent(data.get(EVENT)); break; case
     * CONTROL_CHANGE: controlEventHandler.handleEvent(data.get(EVENT)); break; default:
     * log.error("Invalid event received: {}", data); deleteRecord = false; }
     */
    PushMessage pushMessage = mapper.convertValue(data, PushMessage.class);
    pushMessageService.pushMessageOnChannel(pushMessage);
    redisTemplate.opsForStream().acknowledge(consumerGroup, message);
    redisTemplate.opsForStream().delete(message);

  }
}
