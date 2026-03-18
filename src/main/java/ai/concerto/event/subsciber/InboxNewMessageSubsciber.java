package ai.concerto.event.subsciber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InboxNewMessageSubsciber implements MessageListener {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  @Value("${spring.redis.stream.consumer-group}")
  private String consumerGroup;

  @SuppressWarnings("unchecked")
  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      Map<String, String> data = objectMapper.readValue(message.toString(), Map.class);
      log.debug("Received message from new message pubsub for project: {}", data.get("project_id"));
      messagingTemplate.convertAndSendToUser(data.get("project_id"), "/queue/inbox/newmessage",
          message.toString());
    } catch (MessagingException | JsonProcessingException e) {
      log.error("Unable to process inbox v1 conversations event: {}", e);
    }
  }
}
