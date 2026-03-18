package ai.concerto.event.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.handler.BotResponseHandlerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserRequestStreamService extends UserRequestService {

  @Autowired
  @Qualifier("objectMapper")
  private ObjectMapper objectMapper;

  @Autowired
  private BotResponseHandlerFactory botResponseHandlerFactory;

  @Override
  public Object processUserRequest(String applicationId, Object request, Channel channel,
      String vendor) throws Exception {
    Object botResponse = super.processUserRequest(applicationId, request, channel, vendor);
    if (!ObjectUtils.isEmpty(botResponse)) {
      botResponseHandlerFactory.getBotResponseHandler(channel, vendor).postBotResponse(botResponse);
    }

    return botResponse;
  }
}
