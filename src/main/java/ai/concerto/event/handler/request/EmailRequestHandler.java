package ai.concerto.event.handler.request;

import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.ClientType;
import ai.concerto.event.enums.Source;
import ai.concerto.event.exception.RequestHandlerException;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.EmailIntegration;
import ai.concerto.event.handler.UserRequestHandler;
import ai.concerto.event.service.IntegrationService;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Slf4j
@Service
public class EmailRequestHandler implements UserRequestHandler {

  @Autowired
  private IntegrationService integrationService;

  @SuppressWarnings("unchecked")
  @SneakyThrows
  @Override
  public DERequest getDeRequest(String applicationId, Object request) {
    ApplicationIntegration integration =
        integrationService.getApplicationIntegration(applicationId);
    EmailIntegration channelIntegration = (EmailIntegration) integrationService
        .getChannelIntegration(applicationId, Channel.EMAIL, integration);

    if (ObjectUtils.isEmpty(channelIntegration))
      throw new RequestHandlerException("Email channel is disabled!");

    Map<String, String> emailRequest = (Map<String, String>) request;

    DERequest deRequest = new DERequest();

    deRequest.setProjectId(applicationId);
    deRequest = integrationService.updateIntegrationDetails(deRequest, integration);

    deRequest.setUserId(emailRequest.get("userId"));
    deRequest.setEmail(emailRequest.get("userId"));
    deRequest.setUserInputLast(emailRequest.get("userInputLast"));
    deRequest.setSource(Source.email);
    deRequest.setSendEmail(Boolean.TRUE);
    deRequest.setHost(emailRequest.get("host"));

    deRequest.setClientType(ClientType.NON_CONVERSATIONAL.getName());
    deRequest.setQAEnable(Boolean.TRUE);
    deRequest.setSendSearchResults(Boolean.FALSE);
    deRequest.setSessionTimeOut(30);

    return deRequest;
  }

}
