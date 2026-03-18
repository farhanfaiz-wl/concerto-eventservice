package ai.concerto.event.service;

import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.dto.AgentDetail;
import ai.concerto.event.dto.ChatClientEvent;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.ControlOwner;
import ai.concerto.event.enums.Source;
import ai.concerto.event.exchange.*;
import ai.concerto.event.handler.response.ChatClientEventResponse;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class AnalyticsService {

  private static final String X_PROJECT_ID = "X-Project-Id";
  private static final String X_APIKEY = "X-APIKEY";
  private static final String CONVERSATION_CONTROL_URI_FORMAT =
      "%s/api/conversations/%s/channel/%s/user/%s/control/internal";
  private static final String USER_DETAILS_URI_FORMAT = "%s/api/user_profile/internal";
  private static final String USER_FEEDBACK_URI_FORMAT =
      "%s/api/turn_logs/user_feedback/turn_id/%s/internal";
  private static final String EMAIL_REQUEST_URI_FORMAT =
      "%s/api/turn_logs/email_requests/turn_id/%s/internal";
  private static final String GET_TURNLOGS_URI_FORMAT =
      "%s/api/turn_logs/%s/internal?page=%s&per_page=%s";
  private static final String DELETE_TURNLOGS_URI_FORMAT = "%s/api/turn_logs/%s/internal";

  private static final String MULTI_APP_KEY_PREFIX = "evs:multi_application:%s:%s:%s";

  @Autowired
  private ServiceDetails serviceDetails;

  @Autowired
  private RestUtils restUtils;

  @Autowired
  private AnalyticsStreamPublisher streamPublisher;

  @Autowired
  private ObjectMapper snakeCaseMapper;

  @Autowired
  private SessionService sessionService;

  @Autowired
  private FalconService falconService;

  @Autowired
  private RedisTemplate redisTemplate;

  public StatusResponse takeConversationControl(String applicationId, String channel, String userId,
      String control, AgentDetail agentDetail) throws Exception {
    String requestUri = String.format(CONVERSATION_CONTROL_URI_FORMAT,
        serviceDetails.getAnalytics().getUri(), applicationId, channel, userId);

    Properties headerProperties = new Properties();
    headerProperties.put(X_APIKEY, serviceDetails.getAnalytics().getAppKey());
    headerProperties.put(X_PROJECT_ID, applicationId);

    try {
      AnalyticsControl analyticsControl = new AnalyticsControl();
      analyticsControl.setAgentId(agentDetail);
      if (!StringUtils.hasText(control)) {
        analyticsControl.setOwner(ControlOwner.valueOf(control));
      } else {
        analyticsControl.setOwner(ControlOwner.INBOX_AGENT);
      }

      restUtils.putRequest(requestUri, snakeCaseMapper.writeValueAsString(analyticsControl),
          headerProperties, String.class);

      StatusResponse response = new StatusResponse();
      response.getStatus().setCode(200);
      response.getStatus().setType("Triggered control event");
      response.getStatus().setMessage("Control changed event triggered");
      return response;
    } catch (Exception e) {
      log.error("Unable to transfer control", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Unable to transfer control");
    }
  }

  public AnalyticsControl getConversationControl(String applicationId, String channel,
      String userId) {
    String requestUri = String.format(CONVERSATION_CONTROL_URI_FORMAT,
        serviceDetails.getAnalytics().getUri(), applicationId, channel, userId);

    Properties headerProperties = new Properties();
    headerProperties.put(X_APIKEY, serviceDetails.getAnalytics().getAppKey());
    headerProperties.put(X_PROJECT_ID, applicationId);

    try {
      return restUtils.getRequest(requestUri, headerProperties, AnalyticsControl.class);
    } catch (Exception e) {
      log.error("Unable to get conversation control details {}", e);
      return null;
    }
  }

  public UserDetails getUserDetails(String applicationId, String userId, String channel,
      String tenantId, DERequest deRequest) throws Exception {

    String uri = String.format(USER_DETAILS_URI_FORMAT, serviceDetails.getAnalytics().getUri());
    Map<String, Object> userDetailsRequest = new HashMap<>();
    Map<String, String> additionalAttributes = getAdditionalAttributes(deRequest);
    userDetailsRequest.put("project_id", applicationId);
    userDetailsRequest.put("user_id", userId);
    userDetailsRequest.put("channel", channel);
    userDetailsRequest.put("tenant_id", tenantId);
    userDetailsRequest.put("log_me", deRequest.isLogMe());

    if (Channel.WHATSAPP.getName().equals(channel) || Channel.SMS.getName().equals(channel)
        || Channel.TELEPHONY.getName().equals(channel)) {
      userDetailsRequest.put("phone_number", deRequest.getPhoneNumber());
      userDetailsRequest.put("phone_number_with_country_code",
          deRequest.getPhoneNumberWithCountryCode());
      userDetailsRequest.put("country_code", deRequest.getPhoneNumberCountryCode());
    }
    if (!ObjectUtils.isEmpty(additionalAttributes)) {
      userDetailsRequest.put("extra_user_attributes", additionalAttributes);
    }

    Properties headerProperties = new Properties();
    headerProperties.put(X_APIKEY, serviceDetails.getAnalytics().getAppKey());
    headerProperties.put(X_PROJECT_ID, applicationId);

    return restUtils.postRequest(uri, snakeCaseMapper.writeValueAsString(userDetailsRequest),
        headerProperties, UserDetails.class);
  }

  private Map<String, String> getAdditionalAttributes(DERequest deRequest) {

    if (ObjectUtils.isEmpty(deRequest)) {
      return Collections.emptyMap();
    }
    Map<String, String> extraUserAttributes = new HashMap<>();
    extraUserAttributes.put("first_name", deRequest.getUserFirstName());
    extraUserAttributes.put("last_name", deRequest.getUserLastName());
    extraUserAttributes.put("email", deRequest.getEmail());
    extraUserAttributes.put("profile_pic", deRequest.getUserProfilePic());

    return extraUserAttributes.entrySet().stream().filter(entry -> entry.getValue() != null)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }


  public StatusResponse sendAgentMessage(ExternalAgentReplyRequest replyRequest)
      throws JsonProcessingException {
    StatusResponse response = new StatusResponse();
    AnalyticsControl control = getConversationControl(replyRequest.getApplicationId(),
        replyRequest.getChannel(), replyRequest.getUserId());

    if (!ObjectUtils.isEmpty(control)
        && !replyRequest.getAgentDetail().equals(control.getAgentId())) {
      response.getStatus().setCode(405);
      response.getStatus().setType("Invalid Action");
      response.getStatus().setMessage("Action is not allowed for the given agent");
      return response;
    }

    Map<String, Object> reply = new HashMap<>();
    reply.put("project_id", replyRequest.getApplicationId());
    reply.put("user_id", replyRequest.getUserId());
    reply.put("channel", replyRequest.getChannel());
    reply.put("to_user_id", replyRequest.getUserId());
    reply.put("to_channel", replyRequest.getChannel());
    reply.put("body", replyRequest.getConversation().getReply().getText());
    reply.put("userAccessToken", "eventservice");
    try {
      streamPublisher.publishAgentMessage(snakeCaseMapper.writeValueAsString(reply));
      response.getStatus().setCode(200);
      response.getStatus().setType("Success");
      response.getStatus().setMessage("Message Enqueued");
      return response;
    } catch (JsonProcessingException e) {
      log.error("Unable to enqueue agent message to analytics stream", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to process ");
    }
  }

  public ChatClientEventResponse postChatClientEvent(ChatClientEventRequest eventRequest,
      Boolean logMe) {
    ChatClientEvent event = ChatClientEvent.from(eventRequest);
    event.setTimestampS(LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC));
    UserDetails userDetails = new UserDetails();
    try {
      DERequest deRequest = new DERequest();
      deRequest.setLogMe(false);
      userDetails = getUserDetails(event.getProjectId(), event.getUserId(), Source.html5.name(),
          falconService.getTenantByProjectId(event.getProjectId()).getId(), deRequest);
      event.setSessionId(sessionService.getSession(event.getProjectId(),
          userDetails.getUserProfile().getUniversalUserId(), Channel.CHATBOT).getSessionId());
    } catch (Exception e) {
      log.error(String.format("Unable to get user details for project %s with user %s",
          eventRequest.getProjectId(), event.getUserId()), e);
    }

    if (logMe.booleanValue()) {
      streamPublisher.publishChatClientEvent(event);
    }

    StatusResponse response = new StatusResponse();
    response.getStatus().setCode(200);
    response.getStatus().setType("Success");
    response.getStatus().setMessage("Event Published");

    ChatClientEventResponse chatClientEventResponse = new ChatClientEventResponse();
    chatClientEventResponse.setEvent(event);
    chatClientEventResponse.setStatus(response);
    return chatClientEventResponse;
  }

  @SneakyThrows
  public URI addUserFeedback(String payload) {
    payload = URLDecoder.decode(payload, Charset.defaultCharset().name());
    UserFeedbackRequest userFeedbackRequest = UserFeedbackRequest.decode(payload);
    Map<String, Boolean> requestBody = new HashMap<>();
    requestBody.put("answer_helpful", userFeedbackRequest.getAnswerHelpful());
    postUserFeedback(userFeedbackRequest.getTurnId(),
        snakeCaseMapper.writeValueAsString(requestBody));
    return URI.create(serviceDetails.getAnalytics().getSuccessUrl());
  }

  public void postUserFeedback(String turnId, String payload) {
    try {

      String uri =
          String.format(USER_FEEDBACK_URI_FORMAT, serviceDetails.getAnalytics().getUri(), turnId);
      Properties headerProperties = new Properties();
      headerProperties.put(X_APIKEY, serviceDetails.getAnalytics().getAppKey());

      restUtils.putRequest(uri, payload, headerProperties, String.class);

    } catch (Exception ex) {
      log.error("Error occurred while posting user feedback to analytics {}", ex);
    }

  }

  public void postEmailRequest(String request, String turnId) {
    try {
      Validate.notNull(request);
      final StringBuilder uri = new StringBuilder(
          String.format(EMAIL_REQUEST_URI_FORMAT, serviceDetails.getAnalytics().getUri(), turnId));

      Properties headerProperties = new Properties();
      headerProperties.put(X_APIKEY, serviceDetails.getAnalytics().getAppKey());

      restUtils.putRequest(uri.toString(), request, headerProperties, String.class);

    } catch (Exception e) {
      log.error("Error occurred while posting email request to analytics", e);
    }
  }

  public List<Map<String, Object>> fetchTurnLogsFromAnalytics(String applicationId, Integer pageNo,
      Integer itemsPerPage, Map<String, Object> filter) {
    List<Map<String, Object>> chatbotTurnsList = new ArrayList<>();
    try {
      Validate.notNull(filter);
      final StringBuilder uri = new StringBuilder(String.format(GET_TURNLOGS_URI_FORMAT,
          serviceDetails.getAnalytics().getUri(), applicationId,
          Optional.ofNullable(pageNo).orElse(1), Optional.ofNullable(itemsPerPage).orElse(30)));

      Properties headerProperties = new Properties();
      headerProperties.put(X_APIKEY, serviceDetails.getAnalytics().getAppKey());
      headerProperties.put(X_PROJECT_ID, applicationId);

      String turnsList = restUtils.postRequest(uri.toString(),
          snakeCaseMapper.writeValueAsString(filter), headerProperties, String.class);
      chatbotTurnsList =
          snakeCaseMapper.readValue(turnsList, new TypeReference<List<Map<String, Object>>>() {});

    } catch (Exception e) {
      log.error("Error occurred while posting email request to analytics", e);
    }
    return chatbotTurnsList;
  }

  public void deleteTurnLogsChatbot(String applicationId, String userId) {
    try {

      // clear the multi app response
      redisTemplate.opsForValue().getOperations().delete(
          String.format(MULTI_APP_KEY_PREFIX, applicationId, userId, Channel.CHATBOT.name()));

      StringBuilder uri = new StringBuilder(String.format(DELETE_TURNLOGS_URI_FORMAT,
          serviceDetails.getAnalytics().getUri(), applicationId));

      if (StringUtils.hasText(userId)) {
        uri.append("?user_id=").append(userId);
      }

      Properties headerProperties = new Properties();
      headerProperties.put(X_APIKEY, serviceDetails.getAnalytics().getAppKey());
      headerProperties.put(X_PROJECT_ID, applicationId);

      restUtils.deleteRequest(uri.toString(), headerProperties, String.class);

    } catch (Exception ex) {
      log.error("Error occurred while deleting turn logs from analytics {}", ex);
    }

  }

}
