package ai.concerto.event.handler.request;

import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.ClientType;
import ai.concerto.event.enums.Source;
import ai.concerto.event.exception.ResponseHandlerException;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.ChannelIntegration;
import ai.concerto.event.exchange.GoogleBusinessMessageRequest;
import ai.concerto.event.handler.UserRequestHandler;
import ai.concerto.event.service.IntegrationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Slf4j
@Service
public class GoogleBusinessMessageRequestHandler implements UserRequestHandler {
  @Autowired
  private ObjectMapper snakeCaseMapper;
  @Autowired
  private IntegrationService integrationService;

  @Override
  public DERequest getDeRequest(String applicationId, Object request) {
    ApplicationIntegration integration =
        integrationService.getApplicationIntegration(applicationId);

    ChannelIntegration channelIntegration = integrationService.getChannelIntegration(applicationId,
        Channel.GOOGLE_BUSINESS_MESSAGE, integration);

    if (ObjectUtils.isEmpty(channelIntegration))
      throw new ResponseHandlerException("Google business message channel is disabled!");

    DERequest deRequest = new DERequest();
    deRequest.setProjectId(applicationId);
    deRequest.setClientType(ClientType.RICH_TEXT.getName());
    deRequest.setSource(Source.slack);
    deRequest = integrationService.updateIntegrationDetails(deRequest, integration);
    deRequest.setQAEnable(true);
    if (!ObjectUtils.isEmpty(channelIntegration.getEnableQA())) {
      deRequest.setQAEnable(channelIntegration.getEnableQA());
    }
    deRequest.setSendSearchResults(channelIntegration.getShowSearchResult());
    deRequest.setSessionTimeOut(1800);
    if (!ObjectUtils.isEmpty(channelIntegration.getSessionTimeout())) {
      deRequest.setSessionTimeOut(channelIntegration.getSessionTimeout());
    }
    deRequest.setSessionTimeoutPrompt(channelIntegration.getSessionTimeoutPrompt());

    GoogleBusinessMessageRequest googleMessageRequest =
        snakeCaseMapper.convertValue(request, new TypeReference<GoogleBusinessMessageRequest>() {});
    deRequest.setUserId(googleMessageRequest.getConversationId());
    if (!ObjectUtils.isEmpty(googleMessageRequest.getContext())
        && !ObjectUtils.isEmpty(googleMessageRequest.getContext().getUserInfo())) {
      deRequest.setUserFullName(googleMessageRequest.getContext().getUserInfo().getDisplayName());
    }

    if (!ObjectUtils.isEmpty(googleMessageRequest.getMessage())) {
      deRequest.setUserInputLast(googleMessageRequest.getMessage().getText());
    } else {
      // set user input as postback as message will be empty if user response is from suggestions
      deRequest.setUserInputLast(googleMessageRequest.getSuggestionResponse().getPostbackData());
    }
    return deRequest;
  }
}
