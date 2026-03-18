package ai.concerto.event.handler.request;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.ClientType;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.WidgetIntegration;
import ai.concerto.event.service.IntegrationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WidgetRequestHandler {

  @Autowired
  IntegrationService integrationService;

  public DERequest getDeRequest(String applicationId, String userId, String sessionId,
      String query) {
    ApplicationIntegration integration =
        integrationService.getApplicationIntegration(applicationId);
    WidgetIntegration widgetIntegration = (WidgetIntegration) integrationService
        .getChannelIntegration(applicationId, Channel.WIDGET, integration);

    DERequest deRequest = new DERequest();
    deRequest.setProjectId(applicationId);
    deRequest = integrationService.updateIntegrationDetails(deRequest, integration);
    deRequest.setTurnId(UUID.randomUUID().toString());
    deRequest.setUserInputLast(query);
    deRequest.setUserId(userId);
    deRequest.setLogMe(Boolean.FALSE);

    if (!StringUtils.hasText(sessionId)) {
      deRequest.setSessionNew(Boolean.TRUE);
      deRequest.setSessionId(UUID.randomUUID().toString());
    } else {
      deRequest.setSessionNew(Boolean.FALSE);
      deRequest.setSessionId(sessionId);
    }

    if (!ObjectUtils.isEmpty(widgetIntegration)) {
      deRequest.setSendSearchResults(widgetIntegration.getShowSearchResult());
      if (!ObjectUtils.isEmpty(widgetIntegration.getEnableQA())) {
        deRequest.setQAEnable(widgetIntegration.getEnableQA());
      } else {
        deRequest.setQAEnable(true);
      }
    }
    deRequest.setClientType(ClientType.NON_CONVERSATIONAL.getName());
    return deRequest;
  }

}
