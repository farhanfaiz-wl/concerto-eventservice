package ai.concerto.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ai.concerto.event.dto.UserRequestEvent;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.DataSetSchema;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ChatBotSearchResponse;
import ai.concerto.event.exchange.DataSet;
import ai.concerto.event.handler.request.WidgetRequestHandler;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.render.WidgetRenderer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.util.BadRequestException;
import java.security.Principal;
import java.util.*;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;



class ChatBotServiceTest {

  @InjectMocks
  @Resource
  public ChatBotService chatBotService;
  @Mock
  private UserRequestStreamService userRequestStreamService;
  @Mock
  private SimpUserRegistry simpUserRegistry;
  @Mock
  private SimpMessagingTemplate messagingTemplate;
  @Mock
  private FalconService falconService;
  @Mock
  private IntegrationService integrationService;
  @Mock
  private NLUService nluService;
  @Mock
  private SereneService sereneService;
  @Mock
  private AnalyticsService analyticsService;
  @Mock
  private WidgetRequestHandler widgetRequestHandler;
  @Mock
  private WidgetRenderer widgetRenderer;
  @Mock
  private ObjectMapper objectMapper;
  @Mock
  private DEService deService;
  @Mock
  private AnalyticsStreamPublisher analyticsStreamPublisher;
  @Mock
  private Principal principal;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void processChatBotMessageTest() throws Exception {

    when(principal.getName()).thenReturn("628760f765220500074ff907UserId");
    when(simpUserRegistry.getUser("628760f765220500074ff907UserId")).thenReturn(null);

    UserRequestEvent userRequestEvent =
        new UserRequestEvent("628760f765220500074ff907", "request", Channel.CHATBOT, null, null);
    when(objectMapper.readValue("message", UserRequestEvent.class)).thenReturn(userRequestEvent);

    when(objectMapper.readValue("request", Map.class)).thenReturn(new HashMap<>());
    Map<String, String> request = new HashMap<>();
    request.put("web_socket_user_name", "628760f765220500074ff907UserId");
    when(objectMapper.writeValueAsString(request)).thenReturn("request");
    when(objectMapper.readValue("request", Object.class)).thenReturn("request");

    chatBotService.processChatbotMessage(principal, "message", "sessionId");
    verify(userRequestStreamService).processUserRequest("628760f765220500074ff907", "request",
        Channel.CHATBOT, null);
  }

  @Test
  void getPromotionsForEmptyString() {
    String applicationId = "";
    List<Object> expectedResult = Collections.emptyList();
    List<DataSet> actualResult = chatBotService.getPromotions(applicationId);
    assertEquals(expectedResult, actualResult);
  }

  @Test
  void getPromotionsForValidString() {
    String applicationId = "applicationId";
    List<DataSet> expectedResult = new ArrayList<>();
    when(falconService.getDataSets(applicationId, DataSetSchema.PROMOTION_V1, null))
        .thenReturn(expectedResult);
    List<DataSet> result = chatBotService.getPromotions(applicationId);
    verify(falconService).getDataSets(applicationId, DataSetSchema.PROMOTION_V1, null);
  }

  @Test
  void getQuickLinksForEmptyString() {
    String applicationId = "";
    List<Object> expectedResult = Collections.emptyList();
    List<DataSet> actualResult = chatBotService.getQuickLinks(applicationId);
    assertEquals(expectedResult, actualResult);
  }

  @Test
  void getQuickLinksForValidString() {
    String applicationId = "applicationId";
    List<DataSet> expectedResult = new ArrayList<>();
    when(falconService.getDataSets(applicationId, DataSetSchema.QUICK_LINK_V1, null))
        .thenReturn(expectedResult);
    List<DataSet> result = chatBotService.getQuickLinks(applicationId);
    verify(falconService).getDataSets(applicationId, DataSetSchema.QUICK_LINK_V1, null);
  }

  @Test
  void getAutoCompleteSuggestionsTest() throws BadRequestException {
    String applicationId = "applicationId";
    String text = "Text";
    Boolean showSerenResult = false;
    List<NLUService.NLUAutoCompleteResult> nluSearchResult = new ArrayList<>();
    when(nluService.getAutoCompleteResults("applicationId", text)).thenReturn(nluSearchResult);
    List<String> list =
        chatBotService.getAutoCompleteSuggestions(applicationId, text, showSerenResult);
    assertEquals(Collections.emptyList(), list);
    verify(sereneService, never()).getAutoCompleteResults(applicationId, text);
  }

  @Test
  void getAllAnswersTestWithEmptyQuery() {
    String applicationId = "applicationId";
    String userId = "userId";
    String sessionId = "sessionId";
    String query = "";
    String channel = "channel";
    Boolean logEvent = null;
    ChatBotSearchResponse actualObject = (ChatBotSearchResponse) chatBotService
        .getAllAnswers(applicationId, userId, sessionId, query, channel, logEvent);
    assertEquals(Collections.emptyList(), actualObject.getBotRepliesQAResults());

  }

  @Test
  void getChatBotIntegrationWithoutIntegration() {
    String applicationId = "applicationId";
    when(falconService.getApplicationIntegration(applicationId)).thenReturn(Optional.empty());
    assertNull(chatBotService.getChatBotIntegration(applicationId));
  }

  @Test
  void getChatBotIntegrationWithIntegration() {
    String applicationId = "applicationId";
    when(falconService.getApplicationIntegration(applicationId))
        .thenReturn(Optional.of(new ApplicationIntegration()));
    assertNull(chatBotService.getChatBotIntegration(applicationId));
  }

}


