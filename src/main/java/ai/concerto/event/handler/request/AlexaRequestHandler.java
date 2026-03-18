package ai.concerto.event.handler.request;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.SessionEndedReason;
import com.amazon.ask.model.SessionEndedRequest;
import com.amazon.ask.model.interfaces.alexa.presentation.apl.UserEvent;
import com.amazon.ask.request.RequestHelper;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.ClientType;
import ai.concerto.event.enums.IntentName;
import ai.concerto.event.enums.Source;
import ai.concerto.event.exception.RequestHandlerException;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.AmazonIntegration;
import ai.concerto.event.handler.UserRequestHandler;
import ai.concerto.event.service.IntegrationService;
import ai.concerto.event.utils.JWT;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AlexaRequestHandler implements UserRequestHandler {

  @Autowired
  private IntegrationService integrationService;

  public static final String ALEXA_JSON_ROOT_PATH = "request";

  private static IntentName mapIntentName(String intentName) {
    return switch (intentName) {
      case "AMAZON.StopIntent", "actions.intent.STOP" -> IntentName.StopIntent;
      case "AMAZON.CancelIntent", "actions.intent.CANCEL" -> IntentName.CancelIntent;
      case "AMAZON.HelpIntent", "actions.intent.HELP" -> IntentName.HelpIntent;
      case "Full_Message" -> IntentName.Full_Message;
      default -> IntentName.Unknown;
    };
  }


  @Override
  public DERequest getDeRequest(String applicationId, Object request) {
    ApplicationIntegration integration =
        integrationService.getApplicationIntegration(applicationId);
    AmazonIntegration amazonIntegration = (AmazonIntegration) integrationService
        .getChannelIntegration(applicationId, Channel.AMAZON, integration);

    HandlerInput handlerInput = (HandlerInput) request;

    String sessionId = handlerInput.getRequestEnvelope().getSession().getSessionId();
    if (!StringUtils.hasText(sessionId)) {
      String err = "Empty user session for alexa request";
      log.error(err);
      throw new RequestHandlerException(err);
    }

    StringBuilder msg = new StringBuilder();
    RequestHelper requestHelper = RequestHelper.forHandlerInput(handlerInput);

    DERequest deRequest = new DERequest();
    deRequest.setProjectId(applicationId);
    deRequest.setTurnId(UUID.randomUUID().toString());
    deRequest = integrationService.updateIntegrationDetails(deRequest, integration);
    deRequest.setQAEnable(amazonIntegration.getEnableQA());
    deRequest.setSendSearchResults(amazonIntegration.getShowSearchResult());
    deRequest.setSessionTimeOut(1800);
    deRequest.setClientType(ClientType.VOICE.getName());
    deRequest.setSource(Source.alexa);

    if (!ObjectUtils.isEmpty(requestHelper.getSupportedInterfaces().getAlexaPresentationAPL())) {
      deRequest.setClientType(ClientType.SMART_SCREEN.getName());
      deRequest.setSource(Source.echo_show);
    }

    if (!ObjectUtils.isEmpty(requestHelper.getAccountLinkingAccessToken())) {
      deRequest.setEmail(getEmailFromAccessToken(requestHelper.getAccountLinkingAccessToken()));
      if (!ObjectUtils.isEmpty(amazonIntegration.getSendEmail())) {
        deRequest.setSendEmail(amazonIntegration.getSendEmail());
      } else {
        deRequest.setSendEmail(Boolean.FALSE);
      }
    }

    deRequest.setSkillId(
        handlerInput.getRequestEnvelope().getSession().getApplication().getApplicationId());
    deRequest.setDeviceId(requestHelper.getDeviceId());
    requestHelper.getUserId().ifPresent(deRequest::setUserId);

    if (handlerInput.getRequest() instanceof LaunchRequest) {
      // deRequest.setRequestShouldLinkResultBeReturned(handlerInput.getRequestEnvelopeJson()
      // .path("request").path("shouldLinkResultBeReturned").asBoolean());
      deRequest.setUserInputLast("");
    } else if (handlerInput.getRequest() instanceof IntentRequest) {
      handleAlexaIntentRequest(handlerInput, deRequest);
    } else if (handlerInput.getRequest() instanceof UserEvent) {
      deRequest.setUserInputLast(String.valueOf(handlerInput.getRequestEnvelopeJson()
          .path(ALEXA_JSON_ROOT_PATH).path("arguments").get(1).asInt()));
    } else if (handlerInput.getRequest() instanceof SessionEndedRequest) {
      handleAlexaSessionEndedRequest(handlerInput, deRequest, msg);
    } else {
      msg.append(String.format("Unknown request type %s", handlerInput.getRequest().getType()));
      log.error("Unknown request type: {}", handlerInput.getRequest().getType());
      throw new UnsupportedOperationException(
          "Unknown request type " + handlerInput.getRequest().getType());
    }

    deRequest.setUserLocale(requestHelper.getLocale());
    return deRequest;
  }

  private void handleAlexaSessionEndedRequest(HandlerInput handlerInput, DERequest deRequest,
      StringBuilder msg) {
    String reason =
        handlerInput.getRequestEnvelopeJson().path(ALEXA_JSON_ROOT_PATH).path("reason").asText();
    if (SessionEndedReason.ERROR.name().equals(reason)) {
      final String error =
          handlerInput.getRequestEnvelopeJson().path(ALEXA_JSON_ROOT_PATH).path("error").asText();
      log.error("SessionEnded request received. Reason:{}, error:{}", reason, error);
      msg.append(String.format("[ %s ] SessionEnded request received. Reason:%s, error:%s",
          deRequest.getSkillId(), reason, error));
    } else if (SessionEndedReason.USER_INITIATED.name().equals(reason)) {
      log.info("SessionEnded request received. Reason: {}", reason);
      msg.append(String.format("[ %s ] SessionEnded request received. Unknown reason:%s",
          deRequest.getSkillId(), reason));

    } else {
      log.warn("SessionEnded request received. Unknown reason: {}", reason);
      msg.append(String.format("[%s] SessionEnded request received. Unknown reason: %s",
          deRequest.getSkillId(), reason));
    }
    deRequest.setUserInputLast("exit");
  }

  private void handleAlexaIntentRequest(HandlerInput handlerInput, DERequest deRequest) {
    String requestIntentName = handlerInput.getRequestEnvelopeJson().path(ALEXA_JSON_ROOT_PATH)
        .path("intent").path("name").asText();

    if (IntentName.Full_Message.equals(mapIntentName(requestIntentName))) {
      deRequest.setUserInputLast(handlerInput.getRequestEnvelopeJson().path(ALEXA_JSON_ROOT_PATH)
          .path("intent").path("slots").path("msg_text").path("value").asText());
    } else if (IntentName.StopIntent.equals(mapIntentName(requestIntentName))) {
      deRequest.setUserInputLast("stop");
    } else if (IntentName.CancelIntent.equals(mapIntentName(requestIntentName))) {
      deRequest.setUserInputLast("cancel");
    } else if (IntentName.HelpIntent.equals(mapIntentName(requestIntentName))) {
      deRequest.setUserInputLast("help");
    }
  }

  private String getEmailFromAccessToken(String userAccessToken) {
    if (!StringUtils.hasText(userAccessToken))
      return null;

    try {
      JWT jwt = JWT.parse(userAccessToken);
      if (ObjectUtils.isEmpty(jwt))
        return null;

      String subject = (String) jwt.getJwtBody().get("sub");
      String issuer = (String) jwt.getJwtBody().get("iss");
      if (!StringUtils.hasText(subject))
        return null;

      if (subject.contains("@"))
        return subject;

      if (StringUtils.hasText(issuer) && issuer.contains("amazon"))
        return String.format("%s@amazon.com", subject);
    } catch (Exception e) {
      log.error(String.format("Unable to decode JWT string : %s", userAccessToken), e);
    }

    return null;
  }


}
