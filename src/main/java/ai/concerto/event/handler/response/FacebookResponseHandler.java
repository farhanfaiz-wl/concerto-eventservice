package ai.concerto.event.handler.response;

import ai.concerto.event.enums.Channel;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.FacebookIntegration;
import ai.concerto.event.exchange.BotFacebookResponse;
import ai.concerto.event.exchange.MessageInfo;
import ai.concerto.event.handler.BotResponseHandler;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.service.IntegrationService;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class FacebookResponseHandler implements BotResponseHandler {

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FbRecipient {
    private String id;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FbLiveAgentParams {
    private FbRecipient recipient = new FbRecipient();
    private String target_app_id;
    private String metadata;
  }

  private static final String FB_URI_FORMAT = "https://graph.facebook.com/v3.2/me/messages";

  private static final String GIVE_CONTROL_TO_FB_LIVE_AGENT_URI_FORMAT =
      "https://graph.facebook.com/v3.2/me/pass_thread_control";
  public static final String GET_CONTROL_FROM_FB_LIVE_AGENT_URI_FORMAT =
      "https://graph.facebook.com/v2.6/me/take_thread_control?access_token=%s";

  @Autowired
  private RestUtils restUtils;

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private RedisTemplate redisTemplate;

  @Autowired
  private IntegrationService integrationService;

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;



  @Override
  public Object postBotResponse(Object response) {
    BotFacebookResponse facebookResponse = (BotFacebookResponse) response;
    if (!ObjectUtils.isEmpty(facebookResponse)) {
      try {
        String accessToken = getAccessPageToken(facebookResponse.getProjectId());
        log.info("Recieved PageAcessToken for project {}", facebookResponse.getProjectId());

        restUtils.postRequest(FB_URI_FORMAT + "?access_token=" + accessToken,
            mapper.writeValueAsString(facebookResponse), MediaType.APPLICATION_JSON, null,
            String.class);

        addResponseTime();
        log.info("SuccessFully posted the message to facebook");
      } catch (Exception ex) {
        log.error("Unable to send fb message ,{}", ex);
        analyticsStreamPublisher.publishErrorLogssAnalytics(facebookResponse, ex.getMessage());
      }
    }
    return response;
  }

  /***
   *
   * @param recipientId
   * @param projectId
   * @param targetAppId
   * @param response
   * @return
   */
  public void giveControlToLiveAgent(final String recipientId, String projectId, String targetAppId,
      Object response) {
    // We will need the access token to hit the correct FB messenger page...
    try {
      String accessToken = getAccessPageToken(projectId);

      if (!StringUtils.hasText(targetAppId)) {
        targetAppId = "263902037430900";
      }

      FbLiveAgentParams fbLiveAgentParams = new FbLiveAgentParams();
      fbLiveAgentParams.getRecipient().setId(recipientId);
      fbLiveAgentParams.setMetadata("Landing from 'concerto.ai' bot.");
      fbLiveAgentParams.setTarget_app_id(targetAppId);

      postBotResponse(response);
      restUtils.postRequest(
          GIVE_CONTROL_TO_FB_LIVE_AGENT_URI_FORMAT + "?access_token=" + accessToken,
          mapper.writeValueAsString(fbLiveAgentParams), MediaType.APPLICATION_JSON, null,
          String.class);

    } catch (Exception ex) {
      log.error("Unable to give control to live agent ,{}", ex);
    }
  }

  public Boolean getControlBackFromLiveAgent(String userId, String projectId) {
    String accessToken = getAccessPageToken(projectId);

    log.debug("Getting control back from live agent for projectId {} and userId {}", projectId,
        userId);
    String response = null;
    try {
      String url = String.format(GET_CONTROL_FROM_FB_LIVE_AGENT_URI_FORMAT, accessToken);
      FbLiveAgentParams fbLiveAgentParams = new FbLiveAgentParams();
      fbLiveAgentParams.getRecipient().setId(userId);
      fbLiveAgentParams.setMetadata("Taking control to 'concerto.ai' bot.");

      response = restUtils.postRequest(url, mapper.writeValueAsString(fbLiveAgentParams),
          MediaType.APPLICATION_JSON, null, String.class);
    } catch (Exception e) {
      log.error("Something went wrong with response to FB take back control: {}", e);
    }
    if (!StringUtils.hasLength(response)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No code available");
    }
    return true;
  }

  public String getAccessPageToken(String projectId) {
    String key = "PAGE_ACCESS_TOKEN_" + projectId;
    String pageAccessToken =
        mapper.convertValue(redisTemplate.opsForValue().get(key), new TypeReference<String>() {});
    ApplicationIntegration integration = integrationService.getApplicationIntegration(projectId);
    FacebookIntegration facebookIntegration = (FacebookIntegration) integrationService
        .getChannelIntegration(projectId, Channel.FACEBOOK, integration);
    if (ObjectUtils.isEmpty(pageAccessToken) && !ObjectUtils.isEmpty(facebookIntegration)) {
      pageAccessToken = facebookIntegration.getPageAccessToken();
    }
    return pageAccessToken;
  }

  @Override
  public MessageInfo getMessageInfo(Object response) {
    // TODO Auto-generated method stub
    return new MessageInfo();
  }

}
