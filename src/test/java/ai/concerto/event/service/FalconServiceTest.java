package ai.concerto.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.DataSetSchema;
import ai.concerto.event.exchange.AccountLinkingResponse;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.AuthCodeVerifyResponse;
import ai.concerto.event.exchange.EventSubscriptionDetails;
import ai.concerto.event.utils.Constants;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.web.server.ResponseStatusException;

class FalconServiceTest {

  private static final String X_PROJECT_ID = "X-Project-Id";

  @InjectMocks
  @Resource
  FalconService falconService;

  @Spy
  private ObjectMapper objectMapper;

  @Mock
  private ServiceDetails serviceDetails;

  @Mock
  private RestUtils restUtils;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void getApplicationEventSubscriptionDetailsTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("someUri");
    service.setAppName("appName");
    service.setAppKey("appKey");
    when(serviceDetails.getFalcon()).thenReturn(service);
    Properties headerProperties = new Properties();
    EventSubscriptionDetails subscriptionDetails = new EventSubscriptionDetails();
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_NAME, "appName");
    when(restUtils.getRequest("someUri/v1/project/applicationId/integration/webhook/internal",
        headerProperties, EventSubscriptionDetails.class)).thenReturn(null);
    assertNull(falconService.getApplicationEventSubscriptionDetails("applicationId"));
  }

  @Test
  void getApplicationIntegrationTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("someUri");
    service.setAppKey("someAppKey");
    service.setAppName("someAppName");
    when(serviceDetails.getFalcon()).thenReturn(service);
    Properties headerProperties = new Properties();
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_NAME,
        serviceDetails.getFalcon().getAppName());
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_TOKEN,
        serviceDetails.getFalcon().getAppKey());
    ApplicationIntegration applicationIntegration = new ApplicationIntegration();
    when(restUtils.getRequest("someUri/v1/project/applicationId/integration/internal",
        headerProperties, ApplicationIntegration.class)).thenReturn(applicationIntegration);

    assertEquals(Optional.class,
        falconService.getApplicationIntegration("applicationId").getClass());
  }

  @Test
  void getNluModelByAppIdTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("http://someUri");
    service.setAppKey("someAppKey");
    service.setAppName("someAppName");
    when(serviceDetails.getFalcon()).thenReturn(service);
    Properties headerProperties = new Properties();
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_NAME,
        serviceDetails.getFalcon().getAppName());
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_TOKEN,
        serviceDetails.getFalcon().getAppKey());
    Map<String, Object> data = new HashMap<>();
    data.put("id", "someId");
    Map<String, Object> params = new HashMap<>();
    params.put("executor_class_name", "someName");
    params.put("method_class_name", "methodName");
    params.put("method_id", "methodIds");
    data.put("nlu_model", params);
    Map<String, Object> modelIds = new HashMap<>();
    modelIds.put("model_ids", "someModelIds");
    data.put("model_ids", modelIds);
    when(restUtils.getRequest(" http://someUri/v1/model/deployed?project_id=applicationId",
        headerProperties, Map.class)).thenReturn(data);

    assertEquals(Optional.class, falconService.getNluModelByAppId("applicationId").getClass());
  }

  @Test
  void AuthCodeVerifyResponseTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("http://someUri");
    service.setAppKey("someAppKey");
    service.setAppName("someAppName");
    when(serviceDetails.getFalcon()).thenReturn(service);
    Properties headerProperties = new Properties();
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_NAME,
        serviceDetails.getFalcon().getAppName());
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_TOKEN,
        serviceDetails.getFalcon().getAppKey());
    headerProperties.put(X_PROJECT_ID, "projectId");

    AuthCodeVerifyResponse authCodeVerifyResponse = new AuthCodeVerifyResponse();
    authCodeVerifyResponse.setCode("code");
    when(restUtils.getRequest(
        "http://someUri/v1/project/projectId/dialog-auth/code?userId=userId&channel=amazon&userEmail=emailId&code=SomeCode",
        headerProperties, AuthCodeVerifyResponse.class)).thenReturn(authCodeVerifyResponse);
    assertEquals(AuthCodeVerifyResponse.class,
        falconService
            .getAuthCodeValidation("projectId", Channel.AMAZON, "userId", "emailId", "SomeCode")
            .getClass());
  }

  @Test
  void AuthCodeVerifyResponseDifferentChannelTest() {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("http://someUri");
    service.setAppKey("someAppKey");
    service.setAppName("someAppName");
    when(serviceDetails.getFalcon()).thenReturn(service);
    assertNull(falconService
        .getAuthCodeValidation("projectId", Channel.CHATBOT, "userId", "emailId", "code")
        .getCode());

  }

  @Test
  void isAmazonAccountLinkingEnabledTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("http://someUri");
    service.setAppKey("someAppKey");
    service.setAppName("someAppName");
    when(serviceDetails.getFalcon()).thenReturn(service);
    Properties headerProperties = new Properties();
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_NAME,
        serviceDetails.getFalcon().getAppName());
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_TOKEN,
        serviceDetails.getFalcon().getAppKey());
    headerProperties.put(X_PROJECT_ID, "projectId");

    when(restUtils.getRequest(
        "http://someUri/v1/project/projectId/alexa_skill/account_linking/internal?stage=DEVELOPMENT",
        headerProperties, AccountLinkingResponse.class)).thenReturn(new AccountLinkingResponse());
    assertEquals(AccountLinkingResponse.class,
        falconService.isAmazonAccountLinkingEnabled("projectId").getClass());
  }

  @Test
  void fetchProjectIdByAlexaSkillIdTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("http://someUri");
    service.setAppKey("someAppKey");
    service.setAppName("someAppName");
    when(serviceDetails.getFalcon()).thenReturn(service);
    Properties headerProperties = new Properties();
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_NAME,
        serviceDetails.getFalcon().getAppName());
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_TOKEN,
        serviceDetails.getFalcon().getAppKey());
    when(restUtils.getRequest(
        "http://someUri/v1/project?skill_type=ALEXA_SKILL&skill_id=alexaSkillId", headerProperties,
        String.class)).thenReturn("{\"project_id\":\"projectId\"}");
    assertEquals("projectId", falconService.fetchProjectIdByAlexaSkillId("alexaSkillId"));
  }

  @Test
  void getRandomAuthCodeTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("http://someUri");
    service.setAppKey("someAppKey");
    service.setAppName("someAppName");
    when(serviceDetails.getFalcon()).thenReturn(service);
    Properties headerProperties = new Properties();
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_NAME,
        serviceDetails.getFalcon().getAppName());
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_TOKEN,
        serviceDetails.getFalcon().getAppKey());
    headerProperties.put(X_PROJECT_ID, "projectId");

    when(restUtils.getRequest("http://someUri/v1/project/projectId/dialog-auth/code/userId",
        headerProperties, String.class)).thenReturn("someResponse");
    assertEquals("someResponse", falconService.getRandomAuthCode("projectId", "userId"));
  }

  @Test
  void getRandomAuthCodeExceptionTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("http://someUri");
    service.setAppKey("someAppKey");
    service.setAppName("someAppName");
    when(serviceDetails.getFalcon()).thenReturn(service);
    Properties headerProperties = new Properties();
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_NAME,
        serviceDetails.getFalcon().getAppName());
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_TOKEN,
        serviceDetails.getFalcon().getAppKey());
    when(restUtils.getRequest("http://someUri/v1/project/projectId/dialog-auth/code/userId",
        headerProperties, String.class)).thenThrow(ResponseStatusException.class);
    assertThrows(ResponseStatusException.class, () -> {
      falconService.getRandomAuthCode("projectId", "userId");
    });
  }

  @Test
  void getRandomAuthCodeEmptyResponseExceptionTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("http://someUri");
    service.setAppKey("someAppKey");
    service.setAppName("someAppName");
    when(serviceDetails.getFalcon()).thenReturn(service);
    Properties headerProperties = new Properties();
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_NAME,
        serviceDetails.getFalcon().getAppName());
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_TOKEN,
        serviceDetails.getFalcon().getAppKey());
    when(restUtils.getRequest("http://someUri/v1/project/projectId/dialog-auth/code/userId",
        headerProperties, String.class)).thenReturn("");
    assertThrows(ResponseStatusException.class, () -> {
      falconService.getRandomAuthCode("projectId", "userId");
    });
  }

  @Test
  void deleteAuthCodeTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("http://someUri");
    service.setAppKey("someAppKey");
    service.setAppName("someAppName");
    when(serviceDetails.getFalcon()).thenReturn(service);
    Properties headerProperties = new Properties();
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_NAME,
        serviceDetails.getFalcon().getAppName());
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_TOKEN,
        serviceDetails.getFalcon().getAppKey());
    headerProperties.put(X_PROJECT_ID, "projectId");

    when(restUtils.deleteRequest("http://someUri/v1/project/projectId/dialog-auth/code/userId",
        headerProperties, Void.class)).thenReturn(null);
    falconService.deleteAuthCode("projectId", "userId");
    verify(restUtils).deleteRequest("http://someUri/v1/project/projectId/dialog-auth/code/userId",
        headerProperties, Void.class);
  }

  @Test
  void deleteAuthCodeExceptionTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("http://someUri");
    service.setAppKey("someAppKey");
    service.setAppName("someAppName");
    when(serviceDetails.getFalcon()).thenReturn(service);

    Properties headerProperties = new Properties();
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_NAME,
        serviceDetails.getFalcon().getAppName());
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_TOKEN,
        serviceDetails.getFalcon().getAppKey());
    headerProperties.put(X_PROJECT_ID, "projectId");

    when(restUtils.deleteRequest("http://someUri/v1/project/projectId/dialog-auth/code/userId",
        headerProperties, Void.class)).thenThrow(ResponseStatusException.class);
    assertThrows(ResponseStatusException.class,
        () -> falconService.deleteAuthCode("projectId", "userId"));
  }

  @Test
  void getDataSets() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("http://someUri");
    service.setAppKey("someAppKey");
    service.setAppName("someAppName");
    when(serviceDetails.getFalcon()).thenReturn(service);

    Properties headerProperties = new Properties();
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_NAME,
        serviceDetails.getFalcon().getAppName());
    headerProperties.put(Constants.HttpConstants.HEADER_KEY_APP_TOKEN,
        serviceDetails.getFalcon().getAppKey());
    headerProperties.put(X_PROJECT_ID, "applicationId");

    when(restUtils.getRequest(
        "http://someUri/v1/project/applicationId/data_set/internal?schema=PROMOTION_V1&query=query",
        headerProperties, String.class)).thenReturn(
            "[{\"id\":\"someId\",\"schema\":\"PROMOTION_V1\",\"tags\":[],\"triggerTags\":[],\"createdAt\":"
                + 435L + ",\"updatedAt\":" + 458L + "}]");
    assertEquals(1,
        falconService.getDataSets("applicationId", DataSetSchema.PROMOTION_V1, "query").size());
  }


}
