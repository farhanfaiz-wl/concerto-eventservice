package ai.concerto.event.handler.request;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.ClientType;
import ai.concerto.event.enums.Source;
import ai.concerto.event.exception.RequestHandlerException;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.SlackIntegration;
import ai.concerto.event.exchange.SlackRequest;
import ai.concerto.event.handler.UserRequestHandler;
import ai.concerto.event.service.IntegrationService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SlackRequestHandler implements UserRequestHandler {

  @Autowired
  private ObjectMapper snakeCaseMapper;

  @Autowired
  private IntegrationService integrationService;

  @SneakyThrows
  @Override
  public DERequest getDeRequest(String applicationId, Object request) {
    ApplicationIntegration integration =
        integrationService.getApplicationIntegration(applicationId);
    SlackIntegration channelIntegration = (SlackIntegration) integrationService
        .getChannelIntegration(applicationId, Channel.SLACK, integration);

    if (ObjectUtils.isEmpty(channelIntegration))
      throw new RequestHandlerException("Slack channel is disabled!");

    if (!StringUtils.hasText(channelIntegration.getBotToken())) {
      throw new RequestHandlerException("Slack channel token is missing!");
    }

    SlackRequest slackRequest =
        snakeCaseMapper.convertValue(request, new TypeReference<SlackRequest>() {});

    DERequest deRequest = new DERequest();
    deRequest.setProjectId(applicationId);
    deRequest = integrationService.updateIntegrationDetails(deRequest, integration);
    deRequest.setClientType(ClientType.MESSAGING.getName());
    deRequest.setSource(Source.slack);
    deRequest.setQAEnable(channelIntegration.getEnableQA());
    deRequest.setBotAuthToken(channelIntegration.getBotToken());
    deRequest.setSendSearchResults(channelIntegration.getShowSearchResult());
    deRequest.setSessionTimeOut(1800);
    if (!ObjectUtils.isEmpty(channelIntegration.getSessionTimeout())) {
      deRequest.setSessionTimeOut(channelIntegration.getSessionTimeout());
    }
    // TODO: missing session timeout

    JsonNode requestNode = snakeCaseMapper.convertValue(request, new TypeReference<JsonNode>() {});
    deRequest.setSlackChannel(requestNode.get("event").get("channel").asText());

    // TODO: check with navin why are we reading from elemts and not event.text field
    List<Map<String, String>> elements = (List<Map<String, String>>) slackRequest.getEvent()
        .getBlocks().get(0).getElements().get(0).getOrDefault("elements", Collections.emptyList());
    StringBuilder text = new StringBuilder();
    for (Map<String, String> element : elements) {
      if (StringUtils.hasText(element.get("text"))) {
        text.append(element.get("text"));
      }
    }
    String userLastMessage = text.toString();
    if (userLastMessage.length() == 2 && Character.isDigit(userLastMessage.charAt(0))
        && userLastMessage.contains(".")) {
      userLastMessage = userLastMessage.replace(".", "").trim();
    }

    deRequest.setUserInputLast(userLastMessage);
    deRequest.setUserId(slackRequest.getEvent().getUser());

    return deRequest;
  }
}
