package ai.concerto.event.handler.request;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.ClientType;
import ai.concerto.event.enums.Source;
import ai.concerto.event.exception.RequestHandlerException;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.FacebookIntegration;
import ai.concerto.event.exchange.FacebookRequest;
import ai.concerto.event.exchange.FacebookRequest.FacebookMessaging;
import ai.concerto.event.handler.UserRequestHandler;
import ai.concerto.event.handler.response.FacebookResponseHandler;
import ai.concerto.event.service.IntegrationService;
import ai.concerto.event.utils.RestUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FacebookRequestHandler implements UserRequestHandler {

  private static final String FACEBOOK_API_BASE = "https://graph.facebook.com/";
  private static final String FB_DISTORTED_MSG_ERROR =
      "The message received from FB is distorted: ";
  private static final String LIVE_AGENT_FORMATTER = "LIVEAGENT_%s_%s";

  @Autowired
  private ObjectMapper snakeCaseMapper;

  @Autowired
  private IntegrationService integrationService;

  @Autowired
  private RestUtils restUtils;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Autowired
  private FacebookResponseHandler facebookResponseHandler;

  @SneakyThrows
  @Override
  public DERequest getDeRequest(String applicationId, Object request) {
    ApplicationIntegration integration =
        integrationService.getApplicationIntegration(applicationId);
    FacebookIntegration channelIntegration = (FacebookIntegration) integrationService
        .getChannelIntegration(applicationId, Channel.FACEBOOK, integration);

    DERequest deRequest = new DERequest();
    deRequest.setProjectId(applicationId);
    deRequest.setClientType(ClientType.RICH_TEXT.getName());
    deRequest.setSource(Source.facebook);
    deRequest = integrationService.updateIntegrationDetails(deRequest, integration);
    deRequest.setQAEnable(true);
    if (!ObjectUtils.isEmpty(channelIntegration.getEnableQA())) {
      deRequest.setQAEnable(channelIntegration.getEnableQA());
    }
    deRequest.setSendSearchResults(channelIntegration.getShowSearchResult());
    deRequest.setLiveAgentRunning(Boolean.FALSE);
    deRequest.setSessionTimeOut(1800);
    if (!ObjectUtils.isEmpty(channelIntegration.getSessionTimeout())) {
      deRequest.setSessionTimeOut(channelIntegration.getSessionTimeout());
    }
    deRequest.setSessionTimeoutPrompt(channelIntegration.getSessionTimeoutPrompt());

    snakeCaseMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    FacebookRequest fbRequest =
        snakeCaseMapper.convertValue(request, new TypeReference<FacebookRequest>() {});

    FacebookMessaging messaging = null;
    Boolean isLiveAgentRunning = Boolean.FALSE;
    if (!CollectionUtils.isEmpty(fbRequest.getMessaging())) {
      messaging = fbRequest.getMessaging().get(0);
    }
    if (!CollectionUtils.isEmpty(fbRequest.getStandby())) {
      messaging = fbRequest.getStandby().get(0);
      isLiveAgentRunning = Boolean.TRUE;
    }

    if (ObjectUtils.isEmpty(messaging)) {
      String err = FB_DISTORTED_MSG_ERROR + "messaging/standby fields are missing!";
      log.error(err);
      throw new RequestHandlerException(err);
    }

    if (!StringUtils.hasText(messaging.getSender().getId())
        || !StringUtils.hasText(messaging.getRecipient().getId())) {
      String err = FB_DISTORTED_MSG_ERROR + "sender/recipient fields are missing.";
      log.error(err);
      throw new RequestHandlerException(err);
    }

    deRequest.setUserId(messaging.getSender().getId());
    deRequest.setSkillId(messaging.getRecipient().getId());

    String message = null;
    String quickReply = null;

    if (!ObjectUtils.isEmpty(messaging.getMessage())) {
      message = messaging.getMessage().getText();

      // TODO: download attachment and upload to s3 + update the details in de request
    }

    String postback = null;
    if (!ObjectUtils.isEmpty(messaging.getPostback())) {
      postback = messaging.getPostback().getPayload();
    }

    deRequest.setLiveAgentRunning(isLiveAgentRunning);
    deRequest.setUserInputLast(message);

    if (deRequest.isLiveAgentRunning()) {
      String agent =
          String.format(LIVE_AGENT_FORMATTER, deRequest.getUserId(), deRequest.getProjectId());
      String timestampWithTimeoutDuration =
          LocalDateTime.now().toString() + "_" + channelIntegration.getTimeoutDurationInHrs();
      if (!ObjectUtils.isEmpty(channelIntegration.getEnableLiveAgentTimeout())
          && Boolean.TRUE.equals(channelIntegration.getEnableLiveAgentTimeout())) {
        redisTemplate.opsForValue().set(agent, timestampWithTimeoutDuration);
      }
    }

    if (!ObjectUtils.isEmpty(postback)) {
      deRequest.setUserInputLast(postback);

      if ("GET_STARTED".equalsIgnoreCase(postback)) {
        deRequest.setSessionNew(Boolean.TRUE);
        deRequest.setUserInputLast("");
      }
    }

    if (!ObjectUtils.isEmpty(messaging.getMessage())
        && !ObjectUtils.isEmpty(messaging.getMessage().getQuickReply())) {
      quickReply = messaging.getMessage().getQuickReply().getPayload();
      deRequest.setUserInputLast(quickReply);
    }

    Map<String, String> userDetails =
        getUserDetails(messaging.getSender().getId(), channelIntegration.getPageAccessToken());
    if (userDetails.isEmpty()) {
      deRequest.setUserFirstName("Jane Test");
      deRequest.setUserLastName("Doe");
      deRequest.setUserProfilePic("test_pic_profile_address");
    } else {
      deRequest.setUserFirstName(userDetails.get("first_name"));
      deRequest.setUserLastName(userDetails.get("last_name"));
      deRequest.setUserProfilePic(userDetails.get("profile_pic"));
    }
    // TODO: channel quick reply dataset was added, why?

    return deRequest;
  }

  private Map<String, String> getUserDetails(String userId, String accessToken) {
    String url = FACEBOOK_API_BASE + userId
        + "?fields=first_name,last_name,profile_pic&access_token=" + accessToken;
    try {
      String details = restUtils.getRequest(url, new Properties(), String.class);
      return snakeCaseMapper.readValue(details, new TypeReference<Map<String, String>>() {});
    } catch (Exception e) {
      log.error("Unable to get FB user details for userId: {}, with accessToken: {}", userId,
          accessToken, e);
      return Collections.emptyMap();
    }
  }

  @Scheduled(fixedDelay = 3600000)
  public void startLiveAgentCheckScheduler() {
    List<String> keys = getLiveAgentRedisKeys();
    if (!keys.isEmpty()) {
      List<String> deleteAgentList = keys.parallelStream().filter(key -> {
        String value = snakeCaseMapper.convertValue(redisTemplate.opsForValue().get(key),
            new TypeReference<String>() {});
        String[] splitKeys = key.split("_");
        String[] splitValues = value.split("_");
        LocalDateTime timestamp = LocalDateTime.parse(splitValues[0]);
        Duration dur = Duration.between(timestamp, LocalDateTime.now());
        Integer timeoutDuration = 24;
        try {
          timeoutDuration = Integer.parseInt(splitValues[1]);
        } catch (NumberFormatException e) {
          log.error("Unable to get timeout duration for project {}", splitKeys[1], e);
        }

        if (dur.toHours() >= timeoutDuration) {
          return facebookResponseHandler.getControlBackFromLiveAgent(splitKeys[1], splitKeys[2]);
        }
        return false;
      }).toList();

      redisTemplate.delete(deleteAgentList);
    }
  }

  public List<String> getLiveAgentRedisKeys() {
    try {
      Set<String> redisKeys = redisTemplate.keys("*");

      if (!CollectionUtils.isEmpty(redisKeys)) {
        return redisKeys.stream().filter(key -> key.matches("LIVEAGENT(.*)")).toList();
      }
    } catch (Exception e) {
      log.error("Error occurred while fetching list of key by pattern {}", e);
    }
    return Collections.emptyList();
  }
}
