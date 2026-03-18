package ai.concerto.event.handler;

import ai.concerto.event.exchange.MessageInfo;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public interface BotResponseHandler {

  Object postBotResponse(Object response);

  MessageInfo getMessageInfo(Object response);

  default void addResponseTime() {

    if (!ObjectUtils.isEmpty(MDC.get("request_timestamp_ms"))) {
      MDC.put("response_time", String.valueOf(
          System.currentTimeMillis() - Long.valueOf((String) MDC.get("request_timestamp_ms"))));
    }
  }
}
