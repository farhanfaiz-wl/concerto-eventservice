package ai.concerto.event.handler.event;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.exchange.WebHookEvent;
import ai.concerto.event.handler.EventHandler;
import ai.concerto.event.publisher.WebhookEventPublisher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LogEventHandler implements EventHandler {

  @Autowired
  private ObjectMapper snakeCaseMapper;

  @Autowired
  private WebhookEventPublisher publisher;

  @Override
  public void handleEvent(String event) {
    try {
      WebHookEvent logEvent =
          snakeCaseMapper.readValue(event, new TypeReference<WebHookEvent>() {});
      if (ObjectUtils.isEmpty(logEvent.getTimestampS())) {
        logEvent.setTimestampS(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
      }

      publisher.publishEvent(logEvent.getApplicationId(), logEvent.getType(), logEvent);
    } catch (JsonProcessingException e) {
      log.error("Unable to deserialize log event: {}", event, e);
    } catch (Exception e) {
      log.error("Unable to process log event: {}", event, e);
    }
  }
}
