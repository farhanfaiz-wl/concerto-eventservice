package ai.concerto.event.handler.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.ClientType;
import ai.concerto.event.enums.Source;
import ai.concerto.event.exception.RequestHandlerException;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.ChatbotIntegration;
import ai.concerto.event.handler.UserRequestHandler;
import ai.concerto.event.service.IntegrationService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ApiRequestHandler implements UserRequestHandler {
  @Autowired
  private ObjectMapper snakeCaseMapper;

  @Autowired
  private IntegrationService integrationService;

  @SneakyThrows
  @Override
  public DERequest getDeRequest(String applicationId, Object request) {
    DERequest deRequest = snakeCaseMapper.convertValue(request, new TypeReference<DERequest>() {});

    deRequest.setProjectId(applicationId);
    ApplicationIntegration integration =
        integrationService.getApplicationIntegration(applicationId);
    ChatbotIntegration channelIntegration = (ChatbotIntegration) integrationService
        .getChannelIntegration(deRequest.getProjectId(), Channel.HTML5, integration);
    if (ObjectUtils.isEmpty(channelIntegration))
      throw new RequestHandlerException("ChatBot channel is disabled!");

    deRequest = integrationService.updateIntegrationDetails(deRequest, integration);
    deRequest.setClientType(ClientType.VOICE.getName());
    deRequest.setSource(Source.telephony);
    deRequest.setSendSearchResults(Boolean.FALSE);
    deRequest.setQAEnable(Boolean.TRUE);

    deRequest.setSessionTimeOut(1800);
    if (!ObjectUtils.isEmpty(channelIntegration.getSessionTimeout())) {
      deRequest.setSessionTimeOut(channelIntegration.getSessionTimeout());
    }
    deRequest.setSessionTimeoutPrompt(channelIntegration.getSessionTimeoutPrompt());

    return deRequest;
  }


}
