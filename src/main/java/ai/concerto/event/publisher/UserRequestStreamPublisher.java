package ai.concerto.event.publisher;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.dto.UserRequestEvent;
import ai.concerto.event.enums.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserRequestStreamPublisher {

  @Autowired
  private RedisTemplate redisTemplate;

  @Autowired
  private ObjectMapper mapper;


  @Value("${spring.redis.stream.internal.key}")
  private String internalStreamKey;

  @SneakyThrows
  public void publishUserRequest(String applicationId, String request, Channel channel,
      String vendor) {
    UserRequestEvent event =
        UserRequestEvent.builder().applicationId(applicationId).request(request).channel(channel)
            .vendor(vendor).metaData(mapper.writeValueAsString(MDC.getCopyOfContextMap())).build();
    ObjectRecord<String, UserRequestEvent> eventRecord =
        StreamRecords.newRecord().ofObject(event).withStreamKey(internalStreamKey);
    redisTemplate.opsForStream().add(eventRecord);
  }
}
