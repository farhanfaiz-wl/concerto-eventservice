package ai.concerto.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.config.ServiceDetails.Service;
import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.dto.Session;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.ControlOwner;
import ai.concerto.event.enums.Source;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.exchange.AnalyticsControl;
import ai.concerto.event.exchange.BotResponse;
import ai.concerto.event.exchange.UserDetails;
import ai.concerto.event.handler.BotResponseHandlerFactory;
import ai.concerto.event.handler.UserRequestHandlerFactory;
import ai.concerto.event.handler.request.*;
import ai.concerto.event.handler.response.*;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.render.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class UserRequestStreamServiceTest {

  @InjectMocks
  @Resource
  UserRequestStreamService userRequestStreamService;
  @Mock
  private ObjectMapper objectMapper;
  @Mock
  private BotResponseHandlerFactory botResponseHandlerFactory;
  @Mock
  private ChatBotResponseHandler chatBotResponseHandler;
  @Mock
  private SlackResponseHandler slackResponseHandler;
  @Mock
  private WhatsappKaleyraResponseHandler whatsappKaleyraResponseHandler;
  @Mock
  private WhatsappRouteResponseHandler whatsappRouteResponseHandler;
  @Mock
  private WhatsappTwilioResponseHandler whatsappTwilioResponseHandler;
  @Mock
  private WhatsappVFResponseHandler whatsappVFResponseHandler;
  @Mock
  private SmsTwilioResponseHandler smsTwilioResponseHandler;
  @Mock
  private FacebookResponseHandler facebookResponseHandler;
  @Mock
  private UserRequestHandlerFactory handlerFactory;
  @Mock
  private ChatBotRequestHandler chatBotRequestHandler;
  @Mock
  private AnalyticsService analyticsService;
  @Mock
  private SessionService sessionService;
  @Mock
  private ServiceDetails serviceDetails;
  @Mock
  private AnalyticsStreamPublisher analyticsStreamPublisher;
  @Mock
  UserRequestService userRequestService;
  @Mock
  private DEService deService;
  @Mock
  private ResponseRendererFactory rendererFactory;
  @Mock
  private ChatBotRenderer chatBotRenderer;
  @Mock
  private SlackRenderer slackRenderer;
  @Mock
  private WhatsappKaleyraRenderer whatsappKaleyraRenderer;
  @Mock
  private WhatsappRouteRenderer whatsappRouteRenderer;
  @Mock
  private WhatsappTwilioRenderer whatsappTwilioRenderer;
  @Mock
  private WhatsappVFRenderer whatsappVFRenderer;
  @Mock
  private SmsTwilioRenderer smsTwilioRenderer;
  @Mock
  private FacebookRenderer facebookRenderer;
  @Mock
  private ApiRenderer apiRenderer;
  @Mock
  private SlackRequestHandler slackRequestHandler;
  @Mock
  private WhatsappKaleyraRequestHandler whatsappKaleyraRequestHandler;
  @Mock
  private WhatsappRouteRequestHandler whatsappRouteRequestHandler;
  @Mock
  private WhatsappTwilioRequestHandler whatsappTwilioRequestHandler;
  @Mock
  private WhatsappVFRequesthandler whatsappVFRequesthandler;
  @Mock
  private SmsTwilioRequestHandler smsTwilioRequestHandler;
  @Mock
  private FacebookRequestHandler facebookRequestHandler;
  @Mock
  private EmailRequestHandler emailRequestHandler;
  @Mock
  private ApiRequestHandler apiRequestHandler;
  @Mock
  RedisTemplate redisTemplate;

  @Mock
  private ValueOperations valueOperations;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void processUserRequestChatBotTest() throws Exception {
    DERequest deRequest = new DERequest();
    deRequest.setSource(Source.html5);
    deRequest.setUserId("userId");
    deRequest.setUserInputLast("someUserInput");
    deRequest.setUniversalUserId("someUniversalId");
    deRequest.setSessionId("sessionId");
    deRequest.setSessionTimeOut(120);
    deRequest.setLiveAgentRunning(false);
    deRequest.setTicketId("someTicketId");
    deRequest.setTenantId("tenantId");
    deRequest.setPhoneNumber("1234567890");
    deRequest.setPhoneNumberCountryCode("+91");
    deRequest.setPhoneNumberWithCountryCode("+911234567890");
    deRequest.setProjectId("someApplicationId");

    Service heaven = new ServiceDetails().getBase();
    when(serviceDetails.getBase()).thenReturn(heaven);

    when(handlerFactory.getUserRequestHandler(Channel.CHATBOT, null))
        .thenReturn(chatBotRequestHandler);
    Mockito.doReturn(deRequest).when(chatBotRequestHandler).getDeRequest("someApplicationId",
        "request");
    UserDetails userDetails = new UserDetails();
    userDetails.getUserProfile().setUniversalUserId("someUniversalId");
    userDetails.getTicket().setId("someTicketId");
    userDetails.getTicket().setNumber("1");
    userDetails.getControl().setOwner("BOT");
    when(analyticsService.getUserDetails(deRequest.getProjectId(), deRequest.getUserId(),
        Source.html5.name(), "tenantId", deRequest)).thenReturn(userDetails);
    when(analyticsService.getUserDetails(deRequest.getProjectId(), deRequest.getUserId(),
        deRequest.getSource().name(), "tenantId", deRequest)).thenReturn(userDetails);
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("https://heavenUri");
    when(serviceDetails.getBase()).thenReturn(service);
    Session session = new Session();
    session.setSessionId("someSessionId");
    session.setNew(true);
    when(sessionService.getSession(deRequest.getProjectId(), deRequest.getUniversalUserId(),
        Channel.CHATBOT)).thenReturn(session);
    Mockito.doNothing().when(sessionService).updateSessionTtl(
        "evs:session::CHATBOT,someUniversalId,someApplicationId", deRequest.getSessionTimeOut());
    Mockito.doNothing().when(analyticsStreamPublisher).publishTurnLog(deRequest);

    DEBotResponse deResponse = new DEBotResponse();
    deResponse.setInForm(true);
    deRequest.setSlackChannel("");
    session.setNew(false);
    session.setInsideForm(deResponse.isInForm());
    session.setSlackChannel(deRequest.getSlackChannel());
    session.setUserId(deRequest.getUserId());

    when(deService.postDeRequest(deRequest)).thenReturn(deResponse);
    AnalyticsControl analyticsControl = new AnalyticsControl();

    analyticsControl.setOwner(ControlOwner.BOT);
    when(analyticsService.getConversationControl("someApplicationId", Channel.CHATBOT.getName(),
        "userId")).thenReturn(analyticsControl);
    when(rendererFactory.getResponseRenderer(Channel.CHATBOT, null)).thenReturn(chatBotRenderer);
    BotResponse botResponse = new BotResponse();
    when(chatBotRenderer.render("request", deRequest, deResponse)).thenReturn(botResponse);
    when(botResponseHandlerFactory.getBotResponseHandler(Channel.CHATBOT, null))
        .thenReturn(chatBotResponseHandler);
    Mockito.when(chatBotResponseHandler.postBotResponse("request")).thenReturn("messageInfo");
    Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    userRequestStreamService.processUserRequest("someApplicationId", "request", Channel.CHATBOT,
        null);
    verify(botResponseHandlerFactory).getBotResponseHandler(Channel.CHATBOT, null);
    assertEquals(botResponse, userRequestStreamService.processUserRequest("someApplicationId",
        "request", Channel.CHATBOT, null));
  }

  @Test
  void processUserRequestSlackTest() throws Exception {
    DERequest deRequest = new DERequest();
    deRequest.setSource(Source.slack);
    deRequest.setUserId("userId");
    deRequest.setUserInputLast("someUserInput");
    deRequest.setUniversalUserId("someUniversalId");
    deRequest.setSessionId("sessionId");
    deRequest.setSessionTimeOut(120);
    deRequest.setLiveAgentRunning(false);
    deRequest.setTicketId("someTicketId");
    deRequest.setTenantId("tenantId");
    deRequest.setPhoneNumber("1234567890");
    deRequest.setPhoneNumberCountryCode("+91");
    deRequest.setPhoneNumberWithCountryCode("+911234567890");
    deRequest.setProjectId("someApplicationId");

    Service heaven = new ServiceDetails().getBase();
    when(serviceDetails.getBase()).thenReturn(heaven);

    when(handlerFactory.getUserRequestHandler(Channel.SLACK, null)).thenReturn(slackRequestHandler);
    Mockito.doReturn(deRequest).when(slackRequestHandler).getDeRequest("someApplicationId",
        "request");
    UserDetails userDetails = new UserDetails();
    userDetails.getUserProfile().setUniversalUserId("someUniversalId");
    userDetails.getTicket().setId("someTicketId");
    userDetails.getTicket().setNumber("1");
    userDetails.getControl().setOwner("BOT");
    when(analyticsService.getUserDetails(deRequest.getProjectId(), deRequest.getUserId(),
        Source.slack.name(), "tenantId", deRequest)).thenReturn(userDetails);
    when(analyticsService.getUserDetails(deRequest.getProjectId(), deRequest.getUserId(),
        deRequest.getSource().name(), "tenantId", deRequest)).thenReturn(userDetails);
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("https://heavenUri");
    when(serviceDetails.getBase()).thenReturn(service);
    Session session = new Session();
    session.setSessionId("someSessionId");
    session.setNew(true);
    when(sessionService.getSession(deRequest.getProjectId(), deRequest.getUniversalUserId(),
        Channel.SLACK)).thenReturn(session);
    Mockito.doNothing().when(sessionService).updateSessionTtl(
        "evs:session::SLACK,someUniversalId,someApplicationId", deRequest.getSessionTimeOut());
    Mockito.doNothing().when(analyticsStreamPublisher).publishTurnLog(deRequest);
    Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    DEBotResponse deResponse = new DEBotResponse();
    deResponse.setInForm(true);
    deRequest.setSlackChannel("");
    session.setNew(false);
    session.setInsideForm(deResponse.isInForm());
    session.setSlackChannel(deRequest.getSlackChannel());
    session.setUserId(deRequest.getUserId());

    when(deService.postDeRequest(deRequest)).thenReturn(deResponse);
    AnalyticsControl analyticsControl = new AnalyticsControl();

    analyticsControl.setOwner(ControlOwner.BOT);
    when(analyticsService.getConversationControl("someApplicationId", Channel.SLACK.getName(),
        "userId")).thenReturn(analyticsControl);


    when(rendererFactory.getResponseRenderer(Channel.SLACK, null)).thenReturn(slackRenderer);
    BotResponse botResponse = new BotResponse();
    when(slackRenderer.render("request", deRequest, deResponse)).thenReturn(botResponse);
    when(botResponseHandlerFactory.getBotResponseHandler(Channel.SLACK, null))
        .thenReturn(slackResponseHandler);
    Mockito.when(slackResponseHandler.postBotResponse("request")).thenReturn("messageInfo");

    userRequestStreamService.processUserRequest("someApplicationId", "request", Channel.SLACK,
        null);
    verify(botResponseHandlerFactory).getBotResponseHandler(Channel.SLACK, null);
    assertEquals(botResponse, userRequestStreamService.processUserRequest("someApplicationId",
        "request", Channel.SLACK, null));
  }

  @Test
  void processUserRequestWhatsappTest() throws Exception {
    DERequest deRequest = new DERequest();
    deRequest.setSource(Source.whatsapp);
    deRequest.setUserId("userId");
    deRequest.setUserInputLast("someUserInput");
    deRequest.setUniversalUserId("someUniversalId");
    deRequest.setSessionId("sessionId");
    deRequest.setSessionTimeOut(120);
    deRequest.setLiveAgentRunning(false);
    deRequest.setTicketId("someTicketId");
    deRequest.setTenantId("tenantId");
    deRequest.setPhoneNumber("1234567890");
    deRequest.setPhoneNumberCountryCode("+91");
    deRequest.setPhoneNumberWithCountryCode("+911234567890");
    deRequest.setProjectId("someApplicationId");


    Service heaven = new ServiceDetails().getBase();
    when(serviceDetails.getBase()).thenReturn(heaven);

    when(handlerFactory.getUserRequestHandler(Channel.WHATSAPP, String.valueOf(Vendor.KALEYRA)))
        .thenReturn(whatsappKaleyraRequestHandler);
    Mockito.doReturn(deRequest).when(whatsappKaleyraRequestHandler)
        .getDeRequest("someApplicationId", "request");
    UserDetails userDetails = new UserDetails();
    userDetails.getUserProfile().setUniversalUserId("someUniversalId");
    userDetails.getTicket().setId("someTicketId");
    userDetails.getTicket().setNumber("1");
    userDetails.getControl().setOwner("BOT");
    when(analyticsService.getUserDetails(deRequest.getProjectId(), deRequest.getUserId(),
        Source.whatsapp.name(), "tenantId", deRequest)).thenReturn(userDetails);
    when(analyticsService.getUserDetails(deRequest.getProjectId(), deRequest.getUserId(),
        deRequest.getSource().name(), "tenantId", deRequest)).thenReturn(userDetails);
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("https://heavenUri");
    when(serviceDetails.getBase()).thenReturn(service);
    Session session = new Session();
    session.setSessionId("someSessionId");
    session.setNew(true);
    when(sessionService.getSession(deRequest.getProjectId(), deRequest.getUniversalUserId(),
        Channel.WHATSAPP)).thenReturn(session);
    Mockito.doNothing().when(sessionService).updateSessionTtl(
        "evs:session::WHATSAPP,someUniversalId,someApplicationId", deRequest.getSessionTimeOut());
    Mockito.doNothing().when(analyticsStreamPublisher).publishTurnLog(deRequest);
    Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    DEBotResponse deResponse = new DEBotResponse();
    deResponse.setInForm(true);
    deRequest.setSlackChannel("");
    session.setNew(false);
    session.setInsideForm(deResponse.isInForm());
    session.setSlackChannel(deRequest.getSlackChannel());
    session.setUserId(deRequest.getUserId());

    when(deService.postDeRequest(deRequest)).thenReturn(deResponse);
    AnalyticsControl analyticsControl = new AnalyticsControl();

    analyticsControl.setOwner(ControlOwner.BOT);

    when(analyticsService.getConversationControl("someApplicationId", Channel.WHATSAPP.getName(),
        "userId")).thenReturn(analyticsControl);


    when(rendererFactory.getResponseRenderer(Channel.WHATSAPP, String.valueOf(Vendor.KALEYRA)))
        .thenReturn(whatsappKaleyraRenderer);
    BotResponse botResponse = new BotResponse();
    when(whatsappKaleyraRenderer.render("request", deRequest, deResponse)).thenReturn(botResponse);
    when(botResponseHandlerFactory.getBotResponseHandler(Channel.WHATSAPP,
        String.valueOf(Vendor.KALEYRA))).thenReturn(whatsappRouteResponseHandler);
    Mockito.when(whatsappRouteResponseHandler.postBotResponse("request")).thenReturn("messageInfo");

    userRequestStreamService.processUserRequest("someApplicationId", "request", Channel.WHATSAPP,
        String.valueOf(Vendor.KALEYRA));
    verify(botResponseHandlerFactory).getBotResponseHandler(Channel.WHATSAPP,
        String.valueOf(Vendor.KALEYRA));
    assertEquals(botResponse, userRequestStreamService.processUserRequest("someApplicationId",
        "request", Channel.WHATSAPP, String.valueOf(Vendor.KALEYRA)));
  }

  @Test
  void processUserRequestSmsTest() throws Exception {
    DERequest deRequest = new DERequest();
    deRequest.setSource(Source.sms);
    deRequest.setUserId("userId");
    deRequest.setUserInputLast("someUserInput");
    deRequest.setUniversalUserId("someUniversalId");
    deRequest.setSessionId("sessionId");
    deRequest.setSessionTimeOut(120);
    deRequest.setLiveAgentRunning(false);
    deRequest.setTicketId("someTicketId");
    deRequest.setTenantId("tenantId");
    deRequest.setPhoneNumber("1234567890");
    deRequest.setPhoneNumberCountryCode("+91");
    deRequest.setPhoneNumberWithCountryCode("+911234567890");
    deRequest.setProjectId("someApplicationId");


    Service heaven = new ServiceDetails().getBase();
    when(serviceDetails.getBase()).thenReturn(heaven);

    when(handlerFactory.getUserRequestHandler(Channel.SMS, String.valueOf(Vendor.TWILIO)))
        .thenReturn(smsTwilioRequestHandler);
    Mockito.doReturn(deRequest).when(smsTwilioRequestHandler).getDeRequest("someApplicationId",
        "request");
    UserDetails userDetails = new UserDetails();
    userDetails.getUserProfile().setUniversalUserId("someUniversalId");
    userDetails.getTicket().setId("someTicketId");
    userDetails.getTicket().setNumber("1");
    userDetails.getControl().setOwner("BOT");
    when(analyticsService.getUserDetails(deRequest.getProjectId(), deRequest.getUserId(),
        Source.sms.name(), "tenantId", deRequest)).thenReturn(userDetails);
    when(analyticsService.getUserDetails(deRequest.getProjectId(), deRequest.getUserId(),
        deRequest.getSource().name(), "tenantId", deRequest)).thenReturn(userDetails);
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("https://heavenUri");
    when(serviceDetails.getBase()).thenReturn(service);
    Session session = new Session();
    session.setSessionId("someSessionId");
    session.setNew(true);
    when(sessionService.getSession(deRequest.getProjectId(), deRequest.getUniversalUserId(),
        Channel.SMS)).thenReturn(session);
    Mockito.doNothing().when(sessionService).updateSessionTtl(
        "evs:session::SMS,someUniversalId,someApplicationId", deRequest.getSessionTimeOut());
    Mockito.doNothing().when(analyticsStreamPublisher).publishTurnLog(deRequest);

    DEBotResponse deResponse = new DEBotResponse();
    deResponse.setInForm(true);
    deRequest.setSlackChannel("");
    session.setNew(false);
    session.setInsideForm(deResponse.isInForm());
    session.setSlackChannel(deRequest.getSlackChannel());
    session.setUserId(deRequest.getUserId());

    when(deService.postDeRequest(deRequest)).thenReturn(deResponse);
    AnalyticsControl analyticsControl = new AnalyticsControl();

    analyticsControl.setOwner(ControlOwner.BOT);
    when(analyticsService.getConversationControl("someApplicationId", Channel.SMS.getName(),
        "userId")).thenReturn(analyticsControl);

    when(rendererFactory.getResponseRenderer(Channel.SMS, String.valueOf(Vendor.TWILIO)))
        .thenReturn(smsTwilioRenderer);
    BotResponse botResponse = new BotResponse();
    when(smsTwilioRenderer.render("request", deRequest, deResponse)).thenReturn(botResponse);
    when(
        botResponseHandlerFactory.getBotResponseHandler(Channel.SMS, String.valueOf(Vendor.TWILIO)))
            .thenReturn(smsTwilioResponseHandler);
    Mockito.when(smsTwilioResponseHandler.postBotResponse("request")).thenReturn("messageInfo");
    Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    userRequestStreamService.processUserRequest("someApplicationId", "request", Channel.SMS,
        String.valueOf(Vendor.TWILIO));
    verify(botResponseHandlerFactory).getBotResponseHandler(Channel.SMS,
        String.valueOf(Vendor.TWILIO));
    assertEquals(botResponse, userRequestStreamService.processUserRequest("someApplicationId",
        "request", Channel.SMS, String.valueOf(Vendor.TWILIO)));
  }

  @Test
  void processUserRequestFaceBookTest() throws Exception {
    DERequest deRequest = new DERequest();
    deRequest.setSource(Source.facebook);
    deRequest.setUserId("userId");
    deRequest.setUserInputLast("someUserInput");
    deRequest.setUniversalUserId("someUniversalId");
    deRequest.setSessionId("sessionId");
    deRequest.setSessionTimeOut(120);
    deRequest.setLiveAgentRunning(false);
    deRequest.setTicketId("someTicketId");
    deRequest.setTenantId("tenantId");
    deRequest.setPhoneNumber("1234567890");
    deRequest.setPhoneNumberCountryCode("+91");
    deRequest.setPhoneNumberWithCountryCode("+911234567890");
    deRequest.setProjectId("someApplicationId");

    Service heaven = new ServiceDetails().getBase();
    when(serviceDetails.getBase()).thenReturn(heaven);

    when(handlerFactory.getUserRequestHandler(Channel.FACEBOOK, null))
        .thenReturn(facebookRequestHandler);
    Mockito.doReturn(deRequest).when(facebookRequestHandler).getDeRequest("someApplicationId",
        "request");
    UserDetails userDetails = new UserDetails();
    userDetails.getUserProfile().setUniversalUserId("someUniversalId");
    userDetails.getTicket().setId("someTicketId");
    userDetails.getTicket().setNumber("1");
    userDetails.getControl().setOwner("BOT");
    when(analyticsService.getUserDetails(deRequest.getProjectId(), deRequest.getUserId(),
        Source.facebook.name(), "tenantId", deRequest)).thenReturn(userDetails);
    when(analyticsService.getUserDetails(deRequest.getProjectId(), deRequest.getUserId(),
        deRequest.getSource().name(), "tenantId", deRequest)).thenReturn(userDetails);
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("https://heavenUri");
    when(serviceDetails.getBase()).thenReturn(service);
    Session session = new Session();
    session.setSessionId("someSessionId");
    session.setNew(true);
    when(sessionService.getSession(deRequest.getProjectId(), deRequest.getUniversalUserId(),
        Channel.FACEBOOK)).thenReturn(session);
    Mockito.doNothing().when(sessionService).updateSessionTtl(
        "evs:session::FACEBOOK,someUniversalId,someApplicationId", deRequest.getSessionTimeOut());
    Mockito.doNothing().when(analyticsStreamPublisher).publishTurnLog(deRequest);

    DEBotResponse deResponse = new DEBotResponse();
    deResponse.setInForm(true);
    deRequest.setSlackChannel("");
    session.setNew(false);
    session.setInsideForm(deResponse.isInForm());
    session.setSlackChannel(deRequest.getSlackChannel());
    session.setUserId(deRequest.getUserId());

    when(deService.postDeRequest(deRequest)).thenReturn(deResponse);
    AnalyticsControl analyticsControl = new AnalyticsControl();

    analyticsControl.setOwner(ControlOwner.BOT);
    when(analyticsService.getConversationControl("someApplicationId", Channel.FACEBOOK.getName(),
        "userId")).thenReturn(analyticsControl);

    when(rendererFactory.getResponseRenderer(Channel.FACEBOOK, null)).thenReturn(facebookRenderer);
    BotResponse botResponse = new BotResponse();
    when(facebookRenderer.render("request", deRequest, deResponse)).thenReturn(botResponse);
    when(botResponseHandlerFactory.getBotResponseHandler(Channel.FACEBOOK, null))
        .thenReturn(facebookResponseHandler);
    Mockito.when(facebookResponseHandler.postBotResponse("request")).thenReturn("messageInfo");
    Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    userRequestStreamService.processUserRequest("someApplicationId", "request", Channel.FACEBOOK,
        null);
    verify(botResponseHandlerFactory).getBotResponseHandler(Channel.FACEBOOK, null);
    assertEquals(botResponse, userRequestStreamService.processUserRequest("someApplicationId",
        "request", Channel.FACEBOOK, null));
  }
}
