package ai.concerto.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.dto.AgentDetail;
import ai.concerto.event.dto.ConversationTurn;
import ai.concerto.event.dto.MessageReply;
import ai.concerto.event.exchange.AnalyticsControl;
import ai.concerto.event.exchange.ExternalAgentReplyRequest;
import ai.concerto.event.exchange.StatusResponse;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.utils.RestUtils;

class AnalyticsServiceTest {
  @InjectMocks
  @Resource
  AnalyticsService analyticsService;

  @Mock
  private ServiceDetails serviceDetails;

  @Mock
  private RestUtils restUtils;

  @Mock
  private AnalyticsStreamPublisher streamPublisher;

  @Mock
  private ObjectMapper snakeCaseMapper;

  @Mock
  private SessionService sessionService;

  @Mock
  private FalconService falconService;

  @Mock
  private RedisTemplate redisTemplate;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void takeConversationControlExceptionTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    AgentDetail agentDetail = new AgentDetail();
    AnalyticsControl analyticsControl = new AnalyticsControl();
    analyticsControl.setAgentId(agentDetail);

    service.setUri("someUri");
    service.setAppKey("someAppKey");
    Properties headerProperties = new Properties();
    headerProperties.put("X-APIKEY", "someAppKey");
    when(serviceDetails.getAnalytics()).thenReturn(service);
    when(snakeCaseMapper.writeValueAsString(analyticsControl)).thenReturn("request");
    when(restUtils.putRequest(
        "someUri/api/conversations/applicationId/channel/SLACK/user/userId/control/internal",
        "request", headerProperties, String.class)).thenReturn("someResponse");

    assertThrows(ResponseStatusException.class, () -> {
      analyticsService.takeConversationControl("applicationId", "SLACK", "userId", "", agentDetail);
    });
  }

  @Test
  void sendAgentMessageTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    AgentDetail agentDetail = new AgentDetail();
    AnalyticsControl analyticsControl = new AnalyticsControl();
    analyticsControl.setAgentId(agentDetail);
    ConversationTurn conversationTurn = new ConversationTurn();
    MessageReply messageReply = new MessageReply();
    messageReply.setText("someText");

    service.setUri("someUri");
    service.setAppKey("someAppKey");
    Properties headerProperties = new Properties();
    headerProperties.put("X_APIKEY", "someAppKey");
    ExternalAgentReplyRequest externalAgentReplyRequest = new ExternalAgentReplyRequest();
    externalAgentReplyRequest.setApplicationId("applicationId");
    externalAgentReplyRequest.setChannel("SLACK");
    externalAgentReplyRequest.setUserId("userId");
    conversationTurn.setReply(messageReply);
    externalAgentReplyRequest.setConversation(conversationTurn);
    Map<String, Object> reply = new HashMap<>();
    reply.put("project_id", externalAgentReplyRequest.getApplicationId());
    reply.put("user_id", externalAgentReplyRequest.getUserId());
    reply.put("channel", externalAgentReplyRequest.getChannel());
    reply.put("to_user_id", externalAgentReplyRequest.getUserId());
    reply.put("to_channel", externalAgentReplyRequest.getChannel());
    reply.put("body", externalAgentReplyRequest.getConversation().getReply().getText());
    reply.put("userAccessToken", "eventservice");
    when(serviceDetails.getAnalytics()).thenReturn(service);

    when(restUtils.getRequest(
        "someUri/api/conversations/applicationId/channel/SLACK/user/userId/control/internal",
        headerProperties, AnalyticsControl.class)).thenReturn(analyticsControl);
    assertEquals(StatusResponse.class,
        analyticsService.sendAgentMessage(externalAgentReplyRequest).getClass());

  }

  @Test
  void postUserFeedbackTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setAppKey("appKey");
    service.setUri("someUri");
    when(serviceDetails.getAnalytics()).thenReturn(service);
    Properties headerProperties = new Properties();
    headerProperties.put("X-APIKEY", serviceDetails.getAnalytics().getAppKey());
    when(restUtils.putRequest("someUri/api/turn_logs/user_feedback/turn_id/turnId/internal",
        "payload", headerProperties, String.class)).thenReturn("someResponse");
    analyticsService.postUserFeedback("turnId", "payload");
    verify(restUtils).putRequest("someUri/api/turn_logs/user_feedback/turn_id/turnId/internal",
        "payload", headerProperties, String.class);
  }

  @Test
  void postEmailRequestTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setAppKey("appKey");
    service.setUri("someUri");
    when(serviceDetails.getAnalytics()).thenReturn(service);
    Properties headerProperties = new Properties();
    headerProperties.put("X-APIKEY", serviceDetails.getAnalytics().getAppKey());
    when(restUtils.putRequest("someUri/api/turn_logs/email_requests/turn_id/turnId/internal",
        "request", headerProperties, String.class)).thenReturn("response");
    analyticsService.postEmailRequest("request", "turnId");
    verify(restUtils).putRequest("someUri/api/turn_logs/email_requests/turn_id/turnId/internal",
        "request", headerProperties, String.class);
  }

  /*
   * @Test void deleteTurnLogsChatBotTest() throws Exception { ServiceDetails.Service service = new
   * ServiceDetails.Service(); service.setAppKey("appKey"); service.setUri("someUri");
   * when(serviceDetails.getAnalytics()).thenReturn(service); Properties headerProperties = new
   * Properties(); headerProperties.put("X-APIKEY", serviceDetails.getAnalytics().getAppKey());
   * when(restUtils.deleteRequest("someUri/api/turn_logs/applicationString/internal?user_id=userId",
   * headerProperties, String.class)).thenReturn("someResponse");
   * analyticsService.deleteTurnLogsChatbot("applicationString", "userId");
   * verify(restUtils).deleteRequest(
   * "someUri/api/turn_logs/applicationString/internal?user_id=userId", headerProperties,
   * String.class);
   * 
   * }
   */
}
