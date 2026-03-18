package ai.concerto.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.dto.Session;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.handler.response.*;
import ai.concerto.event.notification.supplier.ChannelNotificationHandlerSupplier;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;

class SessionServiceTest {
  @InjectMocks
  @Resource
  SessionService sessionService;

  @Mock
  private RedisTemplate redisTemplate;
  @Spy
  private ObjectMapper snakeMaster;
  @Mock
  private IntegrationService integrationService;
  @Mock
  private AnalyticsStreamPublisher analyticsStreamPublisher;
  @Mock
  private WhatsappTwilioResponseHandler whatsappTwilioResponseHandler;
  @Mock
  private WhatsappKaleyraResponseHandler whatsappKaleyraResponseHandler;
  @Mock
  private WhatsappRouteResponseHandler whatsappRouteResponseHandler;
  @Mock
  private WhatsappVFResponseHandler whatsappVFResponseHandler;
  @Mock
  private SmsTwilioResponseHandler smsTwilioResponseHandler;
  @Mock
  private ChatBotResponseHandler chatBotResponseHandler;
  @Mock
  private SlackResponseHandler slackResponseHandler;
  @Mock
  private FacebookResponseHandler facebookResponseHandler;
  @Mock
  private ChannelNotificationHandlerSupplier channelNotificationHandlerSupplier;
  @Mock
  private ValueOperations valueOperations;
  @Mock
  private RedisConnectionFactory redisConnectionFactory;
  @Mock
  private RedisConnection redisConnection;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void getSessionTest() {
    Session session = new Session();
    assertEquals(session.getClass(),
        sessionService.getSession("applicationId", "universalId", Channel.CHATBOT).getClass());
  }

  @Test
  void updateSessionTest() {
    Session session = new Session();
    assertEquals(session.getClass(),
        sessionService.updateSession(new DERequest(), Channel.CHATBOT, session).getClass());
  }

  @Test
  void updateSessionTtlTest() {
    sessionService.updateSessionTtl("key", 124);
    verify(redisTemplate).expire("key", 124, TimeUnit.SECONDS);
  }

  @Test
  void getAllRedisKeysByPatternWithEmptySetTest() {
    when(redisTemplate.getConnectionFactory()).thenReturn(redisConnectionFactory);
    when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
    ScanOptions options = ScanOptions.scanOptions().match("*").build();
    Cursor<byte[]> cursor = redisConnection.scan(options);
    when(redisConnection.scan(options)).thenReturn(cursor);
    assertEquals(Collections.emptyMap(), sessionService.getAllRedisKeysbyPattern("*"));
  }

  /*
   * @Test void getAllRedisByPatternTest(){
   * when(redisTemplate.getConnectionFactory()).thenReturn(redisConnectionFactory);
   * when(redisConnectionFactory.getConnection()).thenReturn(redisConnection); ScanOptions options
   * =ScanOptions.scanOptions().match("*").build(); List<byte[]> byteList=new ArrayList<>();
   * byteList.add("someKey".getBytes(StandardCharsets.UTF_8));
   * 
   * Cursor<byte[]> cursor =redisConnection.scan(options);
   * 
   * when(redisConnection.scan(options)).thenReturn(cursor); // Set<String> set =new HashSet<>(); //
   * set.add("all.*"); // when(redisTemplate.getExpire("all.*",TimeUnit.SECONDS)) //
   * .thenReturn(30L); // when(redisTemplate.keys("*")).thenReturn(set);
   * assertEquals(0,sessionService.getAllRedisKeysbyPattern(".*").size()); }
   * 
   * @Test void sessionTimeoutChecker(){ String
   * key="evs:session::WHATSAPP,7dff9cde-1f85-11ed-9d92-0242ac11000a,62ff074683630f0006ee1323";
   * Map<String,Session> sessionMap=new HashMap<>(); Session session=new Session();
   * sessionMap.put(key,session);
   * when(sessionService.getAllRedisKeysbyPattern("evs:session::*")).thenReturn(sessionMap);
   * when(redisTemplate.getConnectionFactory()).thenReturn(redisConnectionFactory);
   * when(redisTemplate.getConnectionFactory().getConnection()).thenReturn(redisConnection);
   * Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
   * when(valueOperations.get(anyString())) .thenReturn(
   * "{\"@class\":\"ai.concerto.event.dto.Session\",\"session_id\":\"f1e0d9d8-fa18-4952-b796-0ee0856d816d\",\"process_user_utternace\":false,\"new\":true,\"inside_form\":false}"
   * ); ApplicationIntegration applicationIntegration = new ApplicationIntegration();
   * when(integrationService.getApplicationIntegration("62ff074683630f0006ee1323"))
   * .thenReturn(applicationIntegration); ApplicationIntegration.ChatbotIntegration
   * chatBotIntegration=new ApplicationIntegration.ChatbotIntegration();
   * chatBotIntegration.setSessionTimeoutPrompt("default time out");
   * when(integrationService.getChannelIntegration("62ff074683630f0006ee1323", Channel.HTML5,
   * applicationIntegration)) .thenReturn(chatBotIntegration); WhatsAppNotificationHandler
   * whatsAppTimeOutHandler=new WhatsAppNotificationHandler(); Session sessions=new Session();
   * sessions.setSessionId("f1e0d9d8-fa18-4952-b796-0ee0856d816d");
   * when(channelNotificationHandlerSupplier.getChannelTimeOutHandler(Channel.WHATSAPP))
   * .thenReturn(whatsAppTimeOutHandler); sessionService.sessionTimeoutChecker();
   * verify(channelNotificationHandlerSupplier).getChannelTimeOutHandler(Channel.WHATSAPP); }
   * 
   */



}
