package ai.concerto.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import ai.concerto.event.dto.UserInfo;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.BotConversationEndResponse;
import ai.concerto.event.exchange.BotConversationStartRequest;
import ai.concerto.event.exchange.BotConversationStartResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.server.ResponseStatusException;

class BotConversationServiceTest {

  @InjectMocks
  @Resource
  BotConversationService botConversationService;

  @Mock
  private FalconService falconService;
  @Spy
  private ObjectMapper snakeMaster = new ObjectMapper();
  @Mock
  private RedisTemplate redisTemplate;
  @Mock
  private UserRequestService userRequestService;
  @Mock
  SessionService sessionService;
  @Mock
  private ValueOperations valueOperations;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void newConversationExceptionTest() {
    BotConversationStartRequest request = new BotConversationStartRequest();
    request.setIdleTimeoutS(59);
    assertThrows(ResponseStatusException.class,
        () -> botConversationService.newConversation(request));
  }

  @Test
  void newConversationTestEdgeCase() {
    BotConversationStartRequest request = new BotConversationStartRequest();
    request.setIdleTimeoutS(60);
    assertThrows(ResponseStatusException.class,
        () -> botConversationService.newConversation(request));
  }

  @Test
  void newConversationTestWithoutApplicationIntegration() {
    BotConversationStartRequest request = new BotConversationStartRequest();
    request.setIdleTimeoutS(60);
    request.setProjectId("projectId");
    when(falconService.getApplicationIntegration(request.getProjectId()))
        .thenReturn(Optional.empty());
    assertThrows(ResponseStatusException.class,
        () -> botConversationService.newConversation(request));
  }

  @Test
  void deleteConversationTestWithEmptyConversationStr() {
    Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(snakeMaster.convertValue(redisTemplate.opsForValue().get("BOT_SESSION_"),
        new TypeReference<String>() {})).thenReturn("");
    assertThrows(ResponseStatusException.class,
        () -> botConversationService.deleteConversation(""));

  }

  @Test
  void deleteConversationWithValidConversationStr() {
    Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(snakeMaster.convertValue(redisTemplate.opsForValue().get("BOT_SESSION_someString"),
        new TypeReference<String>() {})).thenReturn("BOT_SESSION_someString");
    when(redisTemplate.delete(anyString())).thenReturn(true);
    assertThrows(ResponseStatusException.class,
        () -> botConversationService.deleteConversation("someString"));

  }

  @Test
  void newConversationTest() {
    BotConversationStartRequest request = new BotConversationStartRequest();
    request.setIdleTimeoutS(61);
    request.setProjectId("projectId");
    UserInfo userInfo = new UserInfo();
    userInfo.setId("id");
    userInfo.setFirstName("name");

    request.setUser(userInfo);
    request.setChannel(String.valueOf(Channel.CHATBOT));
    request.setVendor(null);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    when(falconService.getApplicationIntegration(request.getProjectId()))
        .thenReturn(Optional.of(applicationIntegration));
    assertEquals(BotConversationStartResponse.class,
        botConversationService.newConversation(request).getClass());
  }

  @Test
  void deleteConversation() {
    Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("BOT_SESSION_someKey")).thenReturn("someValue");
    when(redisTemplate.delete("BOT_SESSION_someKey")).thenReturn(true);
    when(snakeMaster.convertValue("\"someValue\"", new TypeReference<String>() {}))
        .thenReturn("response");
    assertEquals(BotConversationEndResponse.class,
        botConversationService.deleteConversation("someKey").getClass());
  }

}
