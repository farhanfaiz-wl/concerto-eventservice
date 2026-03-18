package ai.concerto.event.service;

import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.exchange.MultiAppDetails;
import ai.concerto.event.exchange.UserDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class MultiAppService {

  private static final String MULTI_APP_KEY_PREFIX = "evs:multi_application:%s:%s:%s";

  @Autowired
  private SessionService sessionService;

  @SuppressWarnings("rawtypes")
  @Autowired
  private RedisTemplate redisTemplate;

  @Autowired
  private UserRequestService userRequestService;

  @Autowired
  private ObjectMapper snakeMapper;

  @Autowired
  private AnalyticsService analyticsService;

  public MultiAppDetails checkMultiApp(String applicationId, DERequest deRequest, Channel channel)
      throws JsonProcessingException {

    Object multiAppDetails = redisTemplate.opsForValue().get(
        String.format(MULTI_APP_KEY_PREFIX, applicationId, deRequest.getUserId(), channel.name()));
    if (!ObjectUtils.isEmpty(multiAppDetails)) {
      MultiAppDetails appDetails =
          snakeMapper.readValue((String) multiAppDetails, MultiAppDetails.class);
      if (appDetails.getChangedAppId().equals(appDetails.getKeyAppId())
          && appDetails.getChangedAppId().equals(appDetails.getOriginalAppId())
          && !appDetails.isSessionNew()) {
        clearMultiApp(applicationId, deRequest.getUserId(), channel);
        return null;
      }
      return appDetails;

    }
    return null;

  }


  public void clearMultiApp(String applicationId, String userId, Channel channel) {

    redisTemplate.opsForValue().getOperations()
        .delete(String.format(MULTI_APP_KEY_PREFIX, applicationId, userId, channel.name()));
  }

  public void setMultiApp(MultiAppDetails multiAppDetails, Integer timeOutValue) {
    try {

      if (ObjectUtils.isEmpty(timeOutValue)) {
        timeOutValue = 1800;
      }
      clearMultiApp(multiAppDetails.getChangedAppId(), multiAppDetails.getUserId(),
          Channel.valueOf(multiAppDetails.getChannel()));
      redisTemplate.opsForValue().set(
          String.format(MULTI_APP_KEY_PREFIX, multiAppDetails.getKeyAppId(),
              multiAppDetails.getUserId(), multiAppDetails.getChannel()),
          snakeMapper.writeValueAsString(multiAppDetails), timeOutValue, TimeUnit.SECONDS);
    } catch (JsonProcessingException e) {
      log.error("Error occurred while setting up multi app key in redis{}", e);
    }
  }


  public Object getMultiAppResponse(DEBotResponse deResponse, DERequest deRequest, Object request,
      Channel channel, String vendor) throws Exception {


    log.info("Switched to application {}", deResponse.getChangeApp());

    MultiAppDetails multiAppDetails = checkMultiApp(deResponse.getChangeApp(), deRequest, channel);
    if (ObjectUtils.isEmpty(multiAppDetails)) {

      clearSessionAndMultiApp(deRequest.getProjectId(), deResponse.getChangeApp(),
          deRequest.getUserId(), deRequest.getUniversalUserId(), channel, deRequest);
      // fetch the universal userId of changed app clear the session
      UserDetails userDetails = analyticsService.getUserDetails(deResponse.getChangeApp(),
          deRequest.getUserId(), deRequest.getSource().name(), deRequest.getTenantId(), deRequest);
      sessionService.clearSession(deResponse.getChangeApp(),
          userDetails.getUserProfile().getUniversalUserId(), channel, deRequest);
      // set the new value for multi app
      MultiAppDetails newMultiAppDetails = new MultiAppDetails();
      newMultiAppDetails.setChangedAppId(deResponse.getChangeApp());
      newMultiAppDetails.setOriginalAppId(deRequest.getProjectId());
      newMultiAppDetails.setUniversalUserId(deRequest.getUniversalUserId());
      newMultiAppDetails.setUserId(deRequest.getUserId());
      newMultiAppDetails.setChannel(channel.name());
      newMultiAppDetails.setSessionNew(true);
      // set it changed app id for first time
      newMultiAppDetails.setKeyAppId(deResponse.getChangeApp());

      setMultiApp(newMultiAppDetails, deRequest.getSessionTimeOut());
    } else if (!ObjectUtils.isEmpty(multiAppDetails)
        && deResponse.getChangeApp().equals(multiAppDetails.getKeyAppId())) {
      sessionService.clearSession(deRequest.getProjectId(), deRequest.getUniversalUserId(), channel,
          deRequest);
      multiAppDetails.setChangedAppId(deResponse.getChangeApp());
      multiAppDetails.setSessionNew(true);
      setMultiApp(multiAppDetails, deRequest.getSessionTimeOut());

    }
    return userRequestService.processUserRequest(deResponse.getChangeApp(), request, channel,
        vendor);

  }



  private void clearSessionAndMultiApp(String originalAppId, String changedAppId, String userId,
      String universalUserId, Channel channel, DERequest deRequest) {

    // clear the old session
    sessionService.clearSession(originalAppId, universalUserId, channel, deRequest);
    sessionService.clearSession(changedAppId, universalUserId, channel, deRequest);
    // clear the existing multi application for the same user
    redisTemplate.opsForValue().getOperations()
        .delete(String.format(MULTI_APP_KEY_PREFIX, originalAppId, userId, channel.name()));

    redisTemplate.opsForValue().getOperations()
        .delete(String.format(MULTI_APP_KEY_PREFIX, changedAppId, userId, channel.name()));
  }
}
