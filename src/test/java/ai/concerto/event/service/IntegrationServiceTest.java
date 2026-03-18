package ai.concerto.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.dto.NluModel;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.exchange.ApplicationIntegration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class IntegrationServiceTest {

  @InjectMocks
  @Resource
  IntegrationService integrationService;

  @Mock
  FalconService falconService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void updateIntegrationDetailsTest() {
    DERequest deRequest = new DERequest();
    deRequest.setProjectId("projectId");
    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    Map<String, Object> map = new HashMap<>();
    map.put("project_lang", "value");
    map.put("context_switching", false);
    map.put("use_chitchat", true);
    map.put("use_long_answer", false);
    map.put("live_agent", true);
    map.put("disable_recommendations", true);
    map.put("disable_qa_on_message", false);
    map.put("context_switching_message", true);
    map.put("select_correct_appoint_msg", "someString");

    applicationIntegration.setProjectSettings(map);
    applicationIntegration.setTemplate("template");
    NluModel nluModel = new NluModel();

    when(falconService.getApplicationIntegration(deRequest.getProjectId()))
        .thenReturn(Optional.of(applicationIntegration));
    when(falconService.getNluModelByAppId("applicationId")).thenReturn(Optional.of(nluModel));
    assertEquals(DERequest.class,
        integrationService.updateIntegrationDetails(deRequest, applicationIntegration).getClass());
  }

  @Test
  void getCqaModelByAppIdNullTest() {
    when(falconService.getNluModelByAppId("applicationId")).thenReturn(Optional.empty());
    assertNull(integrationService.getCqaModelIdByAppId("applicationId"));
  }

  @Test
  void getCqaModelByAppIdTest() {
    NluModel nluModel = new NluModel();
    nluModel.setCqamodelId("someString");
    when(falconService.getNluModelByAppId("applicationId")).thenReturn(Optional.of(nluModel));
    assertEquals("someString", integrationService.getCqaModelIdByAppId("applicationId"));
  }

  @Test
  void getChannelIntegrationChatBotTest() {
    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    when(falconService.getApplicationIntegration("applicationId"))
        .thenReturn(Optional.of(applicationIntegration));
    ApplicationIntegration.ChatbotIntegration chatbotIntegration =
        new ApplicationIntegration.ChatbotIntegration();
    applicationIntegration.setChatbotIntegration(chatbotIntegration);
    assertEquals(ApplicationIntegration.ChatbotIntegration.class,
        integrationService
            .getChannelIntegration("applicationId", Channel.CHATBOT, applicationIntegration)
            .getClass());
  }

  @Test
  void getChannelIntegrationWhatsAppTest() {
    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    when(falconService.getApplicationIntegration("applicationId"))
        .thenReturn(Optional.of(applicationIntegration));
    ApplicationIntegration.WhatsappIntegration whatsappIntegration =
        new ApplicationIntegration.WhatsappIntegration();
    applicationIntegration.setWhatsappIntegration(whatsappIntegration);
    assertEquals(ApplicationIntegration.WhatsappIntegration.class,
        integrationService
            .getChannelIntegration("applicationId", Channel.WHATSAPP, applicationIntegration)
            .getClass());
  }

  @Test
  void getChannelIntegrationSlack() {
    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    when(falconService.getApplicationIntegration("applicationId"))
        .thenReturn(Optional.of(applicationIntegration));
    ApplicationIntegration.SlackIntegration slackIntegration =
        new ApplicationIntegration.SlackIntegration();
    applicationIntegration.setSlackIntegration(slackIntegration);
    assertEquals(ApplicationIntegration.SlackIntegration.class, integrationService
        .getChannelIntegration("applicationId", Channel.SLACK, applicationIntegration).getClass());
  }

  @Test
  void getChannelIntegrationSmsTest() {
    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    when(falconService.getApplicationIntegration("applicationId"))
        .thenReturn(Optional.of(applicationIntegration));
    ApplicationIntegration.SmsIntegration smsIntegration =
        new ApplicationIntegration.SmsIntegration();
    applicationIntegration.setSmsIntegration(smsIntegration);
    assertEquals(ApplicationIntegration.SmsIntegration.class, integrationService
        .getChannelIntegration("applicationId", Channel.SMS, applicationIntegration).getClass());
  }

  @Test
  void getChannelIntegrationEmailTest() {
    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    when(falconService.getApplicationIntegration("applicationId"))
        .thenReturn(Optional.of(applicationIntegration));
    ApplicationIntegration.EmailIntegration emailIntegration =
        new ApplicationIntegration.EmailIntegration();
    applicationIntegration.setEmailIntegration(emailIntegration);
    assertEquals(ApplicationIntegration.EmailIntegration.class, integrationService
        .getChannelIntegration("applicationId", Channel.EMAIL, applicationIntegration).getClass());
  }

  @Test
  void getChannelIntegrationAmazonTest() {
    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    when(falconService.getApplicationIntegration("applicationId"))
        .thenReturn(Optional.of(applicationIntegration));
    ApplicationIntegration.AmazonIntegration amazonIntegration =
        new ApplicationIntegration.AmazonIntegration();
    applicationIntegration.setAmazonIntegration(amazonIntegration);
    assertEquals(ApplicationIntegration.AmazonIntegration.class, integrationService
        .getChannelIntegration("applicationId", Channel.AMAZON, applicationIntegration).getClass());

  }

  @Test
  void getChannelIntegrationFaceBookTest() {
    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    when(falconService.getApplicationIntegration("applicationId"))
        .thenReturn(Optional.of(applicationIntegration));
    ApplicationIntegration.FacebookIntegration facebookIntegration =
        new ApplicationIntegration.FacebookIntegration();
    applicationIntegration.setFacebookIntegration(facebookIntegration);
    assertEquals(ApplicationIntegration.FacebookIntegration.class,
        integrationService
            .getChannelIntegration("applicationId", Channel.FACEBOOK, applicationIntegration)
            .getClass());
  }

  @Test
  void getChannelIntegrationWebsiteTest() {
    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    when(falconService.getApplicationIntegration("applicationId"))
        .thenReturn(Optional.of(applicationIntegration));
    ApplicationIntegration.WebsiteIntegration websiteIntegration =
        new ApplicationIntegration.WebsiteIntegration();
    applicationIntegration.setWebsiteIntegration(websiteIntegration);
    assertEquals(ApplicationIntegration.WebsiteIntegration.class,
        integrationService
            .getChannelIntegration("applicationId", Channel.WEBSITE, applicationIntegration)
            .getClass());
  }

  @Test
  void getChannelIntegrationWidgetTest() {
    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    when(falconService.getApplicationIntegration("applicationId"))
        .thenReturn(Optional.of(applicationIntegration));
    ApplicationIntegration.WidgetIntegration widgetIntegration =
        new ApplicationIntegration.WidgetIntegration();
    applicationIntegration.setWidgetIntegration(widgetIntegration);
    assertEquals(ApplicationIntegration.WidgetIntegration.class, integrationService
        .getChannelIntegration("applicationId", Channel.WIDGET, applicationIntegration).getClass());
  }
}
