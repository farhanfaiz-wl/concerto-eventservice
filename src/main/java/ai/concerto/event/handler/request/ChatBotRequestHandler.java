package ai.concerto.event.handler.request;

import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.ClientType;
import ai.concerto.event.exception.RequestHandlerException;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.ChannelIntegration;
import ai.concerto.event.handler.UserRequestHandler;
import ai.concerto.event.service.IntegrationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Slf4j
@Service
public class ChatBotRequestHandler implements UserRequestHandler {

  @Autowired
  private ObjectMapper snakeCaseMapper;

  @Autowired
  private IntegrationService integrationService;

  @SneakyThrows
  @Override
  public DERequest getDeRequest(String applicationId, Object request) {
    if (ObjectUtils.isEmpty(MDC.get("metaData"))) {
      setMetaDataForChatBot(applicationId);
    }
    DERequest deRequest = snakeCaseMapper.convertValue(request, new TypeReference<DERequest>() {});
    deRequest.setProjectId(applicationId);
    MDC.put("project_id", applicationId);

    ApplicationIntegration integration =
        integrationService.getApplicationIntegration(deRequest.getProjectId());

    ChannelIntegration channelIntegration = integrationService
        .getChannelIntegration(deRequest.getProjectId(), Channel.CHATBOT, integration);

    deRequest = integrationService.updateIntegrationDetails(deRequest, integration);

    if (ObjectUtils.isEmpty(channelIntegration))
      throw new RequestHandlerException("ChatBot channel is disabled!");

    deRequest.setQAEnable(channelIntegration.getEnableQA());
    deRequest.setSendSearchResults(channelIntegration.getShowSearchResult());
    deRequest.setSessionTimeOut(channelIntegration.getSessionTimeout());
    deRequest.setClientType(ClientType.RICH_TEXT.getName());

    return deRequest;
  }

  private void setMetaDataForChatBot(String applicationId) {
    MDC.put("request_timestamp_ms", String.valueOf(System.currentTimeMillis()));
    MDC.put("correlation_id", UUID.randomUUID().toString());
    MDC.put("channel", "chatbot");
    MDC.put("component_id", "11");
    MDC.put("component_name", "eventservice");
    MDC.put("component_version", "0.1");
    MDC.put("path", String.format("/ws/event/chatbot/%s", applicationId));
  }
}
