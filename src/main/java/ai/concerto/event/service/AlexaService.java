package ai.concerto.event.service;

import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.Source;
import ai.concerto.event.exception.RequestHandlerException;
import ai.concerto.event.exchange.AccountLinkingResponse;
import ai.concerto.event.exchange.AuthCodeVerifyResponse;
import ai.concerto.event.exchange.BotResponse;
import ai.concerto.event.exchange.UserDetails;
import ai.concerto.event.handler.request.AlexaRequestHandler;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.render.AlexaRenderer;
import ai.concerto.event.render.ResponseRendererFactory;
import ai.concerto.event.utils.RestUtils;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.*;
import com.amazon.ask.request.RequestHelper;
import com.amazon.ask.response.ResponseBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class AlexaService {

  private static final String SEND_EMAIL_PATH = "%s/send_email";
  private static final String RESPONSE_VERSION = "1.0";
  private static final String RESPONSE_USER_AGENT = "eventService";
  private static final String SESSION_KEY_PREFIX = "evs:session::";
  private static final String EMAIL = "email:";


  @Autowired
  private DEService deService;

  @Autowired
  private FalconService falconService;

  @Autowired
  private SessionService sessionService;

  @Autowired
  private CertificateVerifierService certificateVerifierService;

  @Autowired
  private AlexaRequestHandler alexaRequestHandler;

  @Autowired
  private ServiceDetails serviceDetails;

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private RestUtils restUtils;

  @Autowired
  private ResponseRendererFactory responseRendererFactory;

  @Autowired
  private AnalyticsService analyticsService;

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;


  @SneakyThrows
  public String handelAlexaRequest(String requestedEvent) {

    log.info("Alexa event={}", requestedEvent);

    JsonNode requestedEventJson = mapper.readTree(requestedEvent);
    RequestEnvelope requestEnvelope = getRequestEnvelope(requestedEventJson);
    HandlerInput handlerInput = HandlerInput.builder().withRequestEnvelope(requestEnvelope)
        .withRequestEnvelopeJson(requestedEventJson).build();
    RequestHelper requestHelper = RequestHelper.forHandlerInput(handlerInput);
    AlexaRenderer alexaRenderer =
        (AlexaRenderer) responseRendererFactory.getResponseRenderer(Channel.AMAZON, null);
    String projectId = falconService.fetchProjectIdByAlexaSkillId(
        requestEnvelope.getSession().getApplication().getApplicationId());
    MDC.put("project_id", projectId);
    DERequest deRequest = alexaRequestHandler.getDeRequest(projectId, handlerInput);
    if (!StringUtils.hasText(projectId)) {
      alexaRenderer.renderErrorMessage(
          String.format("Project not found with given alexa skillId:%s", deRequest.getSkillId()),
          true);
    }

    // setting universal_user_id
    UserDetails userDetails = analyticsService.getUserDetails(projectId, deRequest.getUserId(),
        deRequest.getSource().name(), deRequest.getTenantId(), deRequest);
    deRequest.setUniversalUserId(userDetails.getUserProfile().getUniversalUserId());
    deRequest.setTicketId(userDetails.getTicket().getId());
    deRequest.setUserProfile(userDetails.getUserProfile());

    // handling eventservice session for alexa
    if (!StringUtils.hasText(deRequest.getUserInputLast()) && deRequest.isSessionNew())
      sessionService.clearSession(projectId, deRequest.getUniversalUserId(), Channel.AMAZON,
          deRequest);

    ai.concerto.event.dto.Session session =
        sessionService.getSession(projectId, deRequest.getUniversalUserId(), Channel.AMAZON);

    if (!StringUtils.hasText(deRequest.getSessionId())) {
      deRequest.setSessionId(session.getSessionId());
      deRequest.setSessionNew(requestHelper.isNewSession());
    }

    String sessionKey = SESSION_KEY_PREFIX
        .concat(Arrays.asList(Channel.AMAZON.name(), deRequest.getUniversalUserId(), projectId)
            .stream().collect(Collectors.joining(",")));
    sessionService.updateSessionTtl(sessionKey, deRequest.getSessionTimeOut());


    AccountLinkingResponse accountLinking = falconService.isAmazonAccountLinkingEnabled(projectId);

    if (!ObjectUtils.isEmpty(accountLinking) && !ObjectUtils.isEmpty(accountLinking.getParams())
        && !ObjectUtils.isEmpty(accountLinking.getParams().getSkipOnEnablement())
        && accountLinking.getParams().getSkipOnEnablement().booleanValue()
        && ObjectUtils.isEmpty(requestHelper.getAccountLinkingAccessToken())) {
      return generateUnrecognizedResponse(
          "To start using this skill, please visit the home section of the Alexa web portal on amazon VPN and click on the link to connect with the skill",
          true, true);
    }

    AuthCodeVerifyResponse codeVerifyResponse = falconService.getAuthCodeValidation(projectId,
        Channel.AMAZON, deRequest.getUserId(), deRequest.getEmail(), deRequest.getUserInputLast());
    if (Arrays.asList("success", "disabled").contains(codeVerifyResponse.getStatus())) {
      if (!ObjectUtils.isEmpty(codeVerifyResponse.getCode()))
        deRequest.setUserAuthCode(codeVerifyResponse.getCode());
      DEBotResponse deResponse = deService.postDeRequest(deRequest);
      if (!ObjectUtils.isEmpty(deRequest.getComplianceType())
          && !deRequest.getComplianceType().equals("HIPAA")) {
        log.info("Processed DE Response: {}", deResponse);
      }

      sendEmail(deRequest, deResponse);
      return (String) responseRendererFactory.getResponseRenderer(Channel.AMAZON, null)
          .render(requestedEvent, deRequest, deResponse);
    } else if (Arrays.asList("stop", "cancel", "exit").contains(deRequest.getUserInputLast())) {
      return generateUnrecognizedResponse("Goodbye.", false, true);
    } else if ("welcome".equals(codeVerifyResponse.getStatus())) {
      return generateUnrecognizedResponse(codeVerifyResponse.getSuccessMessage(), false, false);
    } else if ("invalid".equals(codeVerifyResponse.getStatus())) {
      return generateUnrecognizedResponse(codeVerifyResponse.getErrorMessage(), false, false);
    }


    return mapper.writeValueAsString(
        ResponseEnvelope.builder().withVersion(RESPONSE_VERSION).withUserAgent(RESPONSE_USER_AGENT)
            .withResponse(new ResponseBuilder().withSpeech(codeVerifyResponse.getErrorMessage())
                .withShouldEndSession(true).build().orElse(null))
            .build());
  }


  @SneakyThrows
  private String generateUnrecognizedResponse(String text, Boolean withLinkCard,
      Boolean dialogExit) {
    ResponseBuilder responseBuilder = new ResponseBuilder();
    responseBuilder.withSpeech(text).withShouldEndSession(dialogExit);
    if (withLinkCard.booleanValue())
      responseBuilder.withLinkAccountCard();
    return mapper.writeValueAsString(
        ResponseEnvelope.builder().withVersion(RESPONSE_VERSION).withUserAgent(RESPONSE_USER_AGENT)
            .withResponse(responseBuilder.build().orElse(null)).build());
  }


  private String getEmailFromSessionIDAndDeviceIDForAlexaFromBadging(String sessionId,
      String deviceId) {
    try {
      String url = String.format("%s/get_email?session_id=%s&device_id=%s",
          serviceDetails.getAmazonBadging().getUri(), sessionId, deviceId);
      String responseStr = restUtils.getRequest(url, new Properties(), String.class);
      Map<String, Object> response =
          mapper.readValue(responseStr, new TypeReference<Map<String, Object>>() {});
      if (!response.containsKey("data")) {
        log.info("No data found in badging response");
        return null;
      }

      Map<String, Object> data = (Map<String, Object>) response.get("data");
      if (!data.isEmpty() && StringUtils.hasText(data.get(EMAIL).toString()))
        return (String) data.get(EMAIL);

      log.info("Email not found in response data from badging");
      return null;
    } catch (Exception e) {
      log.error("Error in getting email for deviceId: {}, sessionId: {}", deviceId, sessionId, e);
    }
    return null;
  }


  private RequestEnvelope getRequestEnvelope(JsonNode requestEvent) {

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, true);
    objectMapper.registerModule(new JavaTimeModule());

    Context context = objectMapper.convertValue(requestEvent.path("context"), Context.class);
    Session session = objectMapper.convertValue(requestEvent.path("session"), Session.class);
    Request request = objectMapper.convertValue(requestEvent.path("request"), Request.class);
    String version = requestEvent.path("version").asText();

    return RequestEnvelope.builder().withContext(context).withRequest(request).withSession(session)
        .withVersion(version).build();

  }

  private void sendEmail(DERequest deRequest, DEBotResponse deBotResponse) {


    if (StringUtils.hasText(deRequest.getEmail())
        && Boolean.TRUE.equals(deBotResponse.getSendEmail())) {
      String email = deRequest.getEmail();
      String userInputLast = deRequest.getUserInputLast();
      String dgiInputLast = deBotResponse.getQuestionForEmail();

      String botRepliesEmail = deBotResponse.getMailReply();
      Map<String, Object> sendEmailRequest = Maps.newHashMap();
      sendEmailRequest.put(EMAIL, email);
      sendEmailRequest.put("answer", botRepliesEmail);
      if (!StringUtils.hasText(dgiInputLast)) {
        sendEmailRequest.put("question", userInputLast);
      } else {
        sendEmailRequest.put("question", dgiInputLast);
      }
      sendEmailRequest.put("project_id", deRequest.getProjectId());

      try {

        String requestUri =
            String.format(SEND_EMAIL_PATH, serviceDetails.getEmailClient().getUri());
        Properties headerProperties = new Properties();

        restUtils.postRequest(requestUri, mapper.writeValueAsString(sendEmailRequest),
            headerProperties, null);
      } catch (Exception ex) {
        log.error("Error occurred while sending email{}", ex);
      }

    }

  }


  public String processAlexaRequest(String event, String appName, String signatureCertChainUrl,
      String signature) throws ResponseStatusException {
    String response = null;
    try {
      JsonNode requestEvent = mapper.readValue(event, JsonNode.class);
      if (StringUtils.hasText(appName)) {
        response = handelAlexaRequest(event);
      } else if (validateRequestHeaders(signatureCertChainUrl, signature, requestEvent)) {
        response = handelAlexaRequest(event);
      } else {
        throw new RequestHandlerException("Bad request");
      }

    } catch (Exception ex) {
      log.error("Exception in processing alexa request", ex);
      analyticsStreamPublisher.publishErrorLogssAnalytics(getBotResponse(event), ex.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    return response;
  }


  private BotResponse getBotResponse(String event) {
    BotResponse botResponse = new BotResponse();
    JsonNode requestedEventJson;
    try {
      requestedEventJson = mapper.readTree(event);
      RequestEnvelope requestEnvelope = getRequestEnvelope(requestedEventJson);
      botResponse.setProjectId(falconService.fetchProjectIdByAlexaSkillId(
          requestEnvelope.getSession().getApplication().getApplicationId()));
      botResponse.setSource(Source.alexa.name());
    } catch (Exception e) {
      log.error("Error in generating BotResponse for Alexa", e);
    }
    return botResponse;
  }


  private boolean validateRequestHeaders(String signatureCertChainUrl, String signature,
      JsonNode event) {
    boolean isValid = true;
    try {
      certificateVerifierService.checkRequestSignature(event.toString(), signature,
          signatureCertChainUrl);
      certificateVerifierService.verifyTimestamp(event.get("request").get("timestamp").toString(),
          150l);
    } catch (Exception e) {
      isValid = false;
      log.error("Error occured in verifying alexa request", e);
    }
    return isValid;
  }

}
