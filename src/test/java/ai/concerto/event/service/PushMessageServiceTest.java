package ai.concerto.event.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.PushMessage;
import ai.concerto.event.handler.BotResponseHandlerFactory;
import ai.concerto.event.handler.response.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PushMessageServiceTest {
  @InjectMocks
  @Resource
  PushMessageService pushMessageService;

  @Mock
  private BotResponseHandlerFactory botResponseHandlerFactory;

  @Mock
  private IntegrationService integrationService;

  @Mock
  private ObjectMapper mapper;
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

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void pushMessageOnChannelSlackTest() {
    PushMessage pushMessage = new PushMessage();
    pushMessage.setChannel("slack");
    pushMessage.setMessage("hello there");
    pushMessage.setUserId("userId");
    pushMessage.setProjectId("projectId");
    pushMessage.setTurnId("turnUiId");
    pushMessage.setTo("to");
    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    applicationIntegration.setProjectSettings(new HashMap<String, Object>());
    when(integrationService.getApplicationIntegration(pushMessage.getProjectId()))
        .thenReturn(applicationIntegration);
    ApplicationIntegration.SlackIntegration slackIntegration =
        new ApplicationIntegration.SlackIntegration();
    slackIntegration.setBotToken("botToken");
    when(botResponseHandlerFactory.getBotResponseHandler(Channel.SLACK, pushMessage.getVendor()))
        .thenReturn(slackResponseHandler);
    when(integrationService.getChannelIntegration(pushMessage.getProjectId(), Channel.SLACK,
        applicationIntegration)).thenReturn(slackIntegration);
    pushMessageService.pushMessageOnChannel(pushMessage);
    verify(botResponseHandlerFactory).getBotResponseHandler(Channel.SLACK, pushMessage.getVendor());

  }

  @Test
  void pushMessageOnChannelWhatsAppKaleyraTest() {
    PushMessage pushMessage = new PushMessage();
    pushMessage.setChannel(String.valueOf(Channel.WHATSAPP));
    pushMessage.setVendor(String.valueOf(Vendor.KALEYRA));
    pushMessage.setMessage("hello there");
    pushMessage.setTo("to");
    pushMessage.setFrom("from");
    pushMessage.setUserId("userId");
    pushMessage.setProjectId("projectId");
    pushMessage.setTurnId("turnUiId");

    ApplicationIntegration.ProviderDetails providerDetails =
        new ApplicationIntegration.ProviderDetails();
    providerDetails.setAccountId("accountID");
    providerDetails.setAuthToken("authToken");
    ApplicationIntegration.ServiceProvider serviceProvider =
        new ApplicationIntegration.ServiceProvider();
    serviceProvider.setProviderDetails(providerDetails);
    serviceProvider.setMessageServiceProvider(Vendor.KALEYRA);

    List<ApplicationIntegration.ServiceProvider> list = new ArrayList<>();
    list.add(serviceProvider);

    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    applicationIntegration.setProjectSettings(new HashMap<String, Object>());
    when(integrationService.getApplicationIntegration(pushMessage.getProjectId()))
        .thenReturn(applicationIntegration);
    ApplicationIntegration.WhatsappIntegration whatsappIntegration =
        new ApplicationIntegration.WhatsappIntegration();
    whatsappIntegration.setServiceProvidersList(list);
    when(botResponseHandlerFactory.getBotResponseHandler(Channel.WHATSAPP, pushMessage.getVendor()))
        .thenReturn(whatsappKaleyraResponseHandler);
    when(integrationService.getChannelIntegration(pushMessage.getProjectId(), Channel.WHATSAPP,
        applicationIntegration)).thenReturn(whatsappIntegration);
    pushMessageService.pushMessageOnChannel(pushMessage);
    verify(botResponseHandlerFactory).getBotResponseHandler(Channel.WHATSAPP,
        pushMessage.getVendor());

  }

  @Test
  void pushMessageOnChannelWhatsAppRouteTest() {
    PushMessage pushMessage = new PushMessage();
    pushMessage.setChannel(String.valueOf(Channel.WHATSAPP));
    pushMessage.setVendor(String.valueOf(Vendor.ROUTE));
    pushMessage.setMessage("hello there");
    pushMessage.setTo("to");
    pushMessage.setFrom("from");
    pushMessage.setUserId("userId");
    pushMessage.setProjectId("projectId");
    pushMessage.setTurnId("turnUiId");

    ApplicationIntegration.ProviderDetails providerDetails =
        new ApplicationIntegration.ProviderDetails();
    providerDetails.setAccountId("accountID");
    providerDetails.setAuthToken("authToken");
    ApplicationIntegration.ServiceProvider serviceProvider =
        new ApplicationIntegration.ServiceProvider();
    serviceProvider.setProviderDetails(providerDetails);
    serviceProvider.setMessageServiceProvider(Vendor.ROUTE);

    List<ApplicationIntegration.ServiceProvider> list = new ArrayList<>();
    list.add(serviceProvider);

    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    applicationIntegration.setProjectSettings(new HashMap<String, Object>());
    when(integrationService.getApplicationIntegration(pushMessage.getProjectId()))
        .thenReturn(applicationIntegration);
    ApplicationIntegration.WhatsappIntegration whatsappIntegration =
        new ApplicationIntegration.WhatsappIntegration();
    whatsappIntegration.setServiceProvidersList(list);
    when(botResponseHandlerFactory.getBotResponseHandler(Channel.WHATSAPP, pushMessage.getVendor()))
        .thenReturn(whatsappKaleyraResponseHandler);
    when(integrationService.getChannelIntegration(pushMessage.getProjectId(), Channel.WHATSAPP,
        applicationIntegration)).thenReturn(whatsappIntegration);
    pushMessageService.pushMessageOnChannel(pushMessage);
    verify(botResponseHandlerFactory).getBotResponseHandler(Channel.WHATSAPP,
        pushMessage.getVendor());

  }

  @Test
  void pushMessageOnChannelWhatsAppTwilioTest() {
    PushMessage pushMessage = new PushMessage();
    pushMessage.setChannel(String.valueOf(Channel.WHATSAPP));
    pushMessage.setVendor(String.valueOf(Vendor.TWILIO));
    pushMessage.setMessage("hello there");
    pushMessage.setTo("to");
    pushMessage.setFrom("from");
    pushMessage.setUserId("userId");
    pushMessage.setProjectId("projectId");
    pushMessage.setTurnId("turnUiId");

    ApplicationIntegration.ProviderDetails providerDetails =
        new ApplicationIntegration.ProviderDetails();
    providerDetails.setAccountId("accountID");
    providerDetails.setAuthToken("authToken");
    ApplicationIntegration.ServiceProvider serviceProvider =
        new ApplicationIntegration.ServiceProvider();
    serviceProvider.setProviderDetails(providerDetails);
    serviceProvider.setMessageServiceProvider(Vendor.TWILIO);

    List<ApplicationIntegration.ServiceProvider> list = new ArrayList<>();
    list.add(serviceProvider);

    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    applicationIntegration.setProjectSettings(new HashMap<String, Object>());
    when(integrationService.getApplicationIntegration(pushMessage.getProjectId()))
        .thenReturn(applicationIntegration);
    ApplicationIntegration.WhatsappIntegration whatsappIntegration =
        new ApplicationIntegration.WhatsappIntegration();
    whatsappIntegration.setServiceProvidersList(list);
    when(botResponseHandlerFactory.getBotResponseHandler(Channel.WHATSAPP, pushMessage.getVendor()))
        .thenReturn(whatsappKaleyraResponseHandler);
    when(integrationService.getChannelIntegration(pushMessage.getProjectId(), Channel.WHATSAPP,
        applicationIntegration)).thenReturn(whatsappIntegration);
    pushMessageService.pushMessageOnChannel(pushMessage);
    verify(botResponseHandlerFactory).getBotResponseHandler(Channel.WHATSAPP,
        pushMessage.getVendor());

  }

  @Test
  void pushMessageOnChannelWhatsAppVfTest() {
    PushMessage pushMessage = new PushMessage();
    pushMessage.setChannel(String.valueOf(Channel.WHATSAPP));
    pushMessage.setVendor(String.valueOf(Vendor.VF));
    pushMessage.setMessage("hello there");
    pushMessage.setTo("to");
    pushMessage.setFrom("from");
    pushMessage.setUserId("userId");
    pushMessage.setProjectId("projectId");
    pushMessage.setTurnId("turnUiId");

    ApplicationIntegration.ProviderDetails providerDetails =
        new ApplicationIntegration.ProviderDetails();
    providerDetails.setAccountId("accountID");
    providerDetails.setAuthToken("authToken");
    ApplicationIntegration.ServiceProvider serviceProvider =
        new ApplicationIntegration.ServiceProvider();
    serviceProvider.setProviderDetails(providerDetails);
    serviceProvider.setMessageServiceProvider(Vendor.VF);

    List<ApplicationIntegration.ServiceProvider> list = new ArrayList<>();
    list.add(serviceProvider);

    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    applicationIntegration.setProjectSettings(new HashMap<String, Object>());
    when(integrationService.getApplicationIntegration(pushMessage.getProjectId()))
        .thenReturn(applicationIntegration);
    ApplicationIntegration.WhatsappIntegration whatsappIntegration =
        new ApplicationIntegration.WhatsappIntegration();
    whatsappIntegration.setServiceProvidersList(list);
    when(botResponseHandlerFactory.getBotResponseHandler(Channel.WHATSAPP, pushMessage.getVendor()))
        .thenReturn(whatsappKaleyraResponseHandler);
    when(integrationService.getChannelIntegration(pushMessage.getProjectId(), Channel.WHATSAPP,
        applicationIntegration)).thenReturn(whatsappIntegration);
    pushMessageService.pushMessageOnChannel(pushMessage);
    verify(botResponseHandlerFactory).getBotResponseHandler(Channel.WHATSAPP,
        pushMessage.getVendor());

  }

  @Test
  void pushMessageOnChannelSmsTest() {
    PushMessage pushMessage = new PushMessage();
    pushMessage.setChannel(String.valueOf(Channel.SMS));
    pushMessage.setVendor(String.valueOf(Vendor.TWILIO));
    pushMessage.setMessage("hello there");
    pushMessage.setTo("to");
    pushMessage.setFrom("from");
    pushMessage.setUserId("userId");
    pushMessage.setProjectId("projectId");
    pushMessage.setTurnId("turnUiId");

    ApplicationIntegration.ProviderDetails providerDetails =
        new ApplicationIntegration.ProviderDetails();
    providerDetails.setAccountId("accountID");
    providerDetails.setAuthToken("authToken");
    ApplicationIntegration.ServiceProvider serviceProvider =
        new ApplicationIntegration.ServiceProvider();
    serviceProvider.setProviderDetails(providerDetails);
    serviceProvider.setMessageServiceProvider(Vendor.TWILIO);

    List<ApplicationIntegration.ServiceProvider> list = new ArrayList<>();
    list.add(serviceProvider);

    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    applicationIntegration.setProjectSettings(new HashMap<String, Object>());
    when(integrationService.getApplicationIntegration(pushMessage.getProjectId()))
        .thenReturn(applicationIntegration);
    ApplicationIntegration.SmsIntegration smsIntegration =
        new ApplicationIntegration.SmsIntegration();
    smsIntegration.setServiceProvidersList(list);
    when(botResponseHandlerFactory.getBotResponseHandler(Channel.SMS, pushMessage.getVendor()))
        .thenReturn(smsTwilioResponseHandler);
    when(integrationService.getChannelIntegration(pushMessage.getProjectId(), Channel.SMS,
        applicationIntegration)).thenReturn(smsIntegration);
    pushMessageService.pushMessageOnChannel(pushMessage);
    verify(botResponseHandlerFactory).getBotResponseHandler(Channel.SMS, pushMessage.getVendor());

  }

  @Test
  void pushMessageOnChannelChatBotTest() {
    PushMessage pushMessage = new PushMessage();
    pushMessage.setChannel(String.valueOf(Channel.HTML5));
    pushMessage.setMessage("hello there");
    pushMessage.setUserId("userId");
    pushMessage.setProjectId("projectId");
    pushMessage.setSessionId("sessionId");
    pushMessage.setTurnId("turnUiId");
    pushMessage.setTo("to");
    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    applicationIntegration.setProjectSettings(new HashMap<String, Object>());
    when(integrationService.getApplicationIntegration(pushMessage.getProjectId()))
        .thenReturn(applicationIntegration);
    when(botResponseHandlerFactory.getBotResponseHandler(Channel.HTML5, pushMessage.getVendor()))
        .thenReturn(chatBotResponseHandler);
    pushMessageService.pushMessageOnChannel(pushMessage);
    verify(botResponseHandlerFactory).getBotResponseHandler(Channel.HTML5, pushMessage.getVendor());
  }


  @Test
  void pushMessageOnChannelFacebookTest() {
    PushMessage pushMessage = new PushMessage();
    pushMessage.setChannel(String.valueOf(Channel.FACEBOOK));
    pushMessage.setMessage("hello there");
    pushMessage.setUserId("userId");
    pushMessage.setProjectId("projectId");
    pushMessage.setTurnId("turnUiId");
    pushMessage.setTo("to");
    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    applicationIntegration.setProjectSettings(new HashMap<String, Object>());
    when(integrationService.getApplicationIntegration(pushMessage.getProjectId()))
        .thenReturn(applicationIntegration);
    when(botResponseHandlerFactory.getBotResponseHandler(Channel.FACEBOOK, pushMessage.getVendor()))
        .thenReturn(facebookResponseHandler);
    pushMessageService.pushMessageOnChannel(pushMessage);
    verify(botResponseHandlerFactory).getBotResponseHandler(Channel.FACEBOOK,
        pushMessage.getVendor());
  }


}
