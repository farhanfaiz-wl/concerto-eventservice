package ai.concerto.event.service;

import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.dto.NluModel;
import ai.concerto.event.dto.ProjectCloneRequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.DataSetSchema;
import ai.concerto.event.exchange.*;
import ai.concerto.event.exchange.StatusResponse.Status;
import ai.concerto.event.utils.Constants.HttpConstants;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.util.*;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class FalconService {

  private static final String X_PROJECT_ID = "X-Project-Id";
  private static final String INTEGRATION_URI_FORMAT = "%s/v1/project/%s/integration/internal";
  private static final String TENANT_BY_PROJECT_ID_URI_FORMAT = "%s/v1/tenant/project/%s";
  private static final String PROJECT_BY_ID_URI_FORMAT = "%s/v1/project/%s/internal";
  private static final String TENANT_CLONE_URI_FORMAT = "%s/v1/tenant/%s/clone/internal";
  private static final String PROJECT_CLONE_URI_FORMAT = "%s/v1/%s/projects/clone/internal";
  private static final String NLU_MODEL_URI_FORMAT = "%s/v1/model/deployed";
  private static final String AUTH_CODE_VERIFY_URI_FORMAT =
      "%s/v1/project/%s/dialog-auth/code?userId=%s&channel=%s";
  private static final String SUBSCRIPTION_URI_FORMAT =
      "%s/v1/project/%s/integration/webhook/internal";
  private static final String AUTH_CODE_URI_FORMAT = "%s/v1/project/%s/dialog-auth/code/%s";
  private static final String ALEXA_SKILL_CACHE_PREFIX = "ALEXA_SKILL_";
  private static final String ALEXA_ACCOUNT_LINKING_URI_FORMAT =
      "%s/v1/project/%s/alexa_skill/account_linking/internal?stage=%s";
  private static final String PROJECT_ID_BY_ALEXA_SKILL_ID_URI =
      "%s/v1/project?skill_type=ALEXA_SKILL&skill_id=%s";
  private static final String DATASET_URI_FORMAT = "%s/v1/project/%s/data_set/internal?schema=%s";
  private static final String WIDGET_CONFIG_BY_ID_URI_FORMAT = "%s/v1/widget/%s/config";
  private static final String X_APP_NAME = "X-APP-NAME";
  private static final String X_APP_TOKEN = "X-APP-TOKEN";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ServiceDetails serviceDetails;

  @Autowired
  private RestUtils restUtils;

  public EventSubscriptionDetails getApplicationEventSubscriptionDetails(String applicationId) {
    String requestUri =
        String.format(SUBSCRIPTION_URI_FORMAT, serviceDetails.getFalcon().getUri(), applicationId);

    Properties headerProperties = new Properties();
    headerProperties.put(HttpConstants.HEADER_KEY_APP_NAME,
        serviceDetails.getFalcon().getAppName());
    headerProperties.put(HttpConstants.HEADER_KEY_APP_TOKEN,
        serviceDetails.getFalcon().getAppKey());
    headerProperties.put(X_PROJECT_ID, applicationId);

    EventSubscriptionDetails subscriptionDetails = new EventSubscriptionDetails();
    try {
      subscriptionDetails =
          restUtils.getRequest(requestUri, headerProperties, EventSubscriptionDetails.class);
    } catch (Exception e) {
      log.error("Unable to call falcon service. Defaulting to unsubscribed behaviour");
      subscriptionDetails.setSubscribedEvents(Collections.emptyList());
    }

    return subscriptionDetails;
  }

  public Optional<ApplicationIntegration> getApplicationIntegration(String applicationId) {
    String requestUri =
        String.format(INTEGRATION_URI_FORMAT, serviceDetails.getFalcon().getUri(), applicationId);

    Properties headerProperties = new Properties();
    headerProperties.put(HttpConstants.HEADER_KEY_APP_NAME,
        serviceDetails.getFalcon().getAppName());
    headerProperties.put(HttpConstants.HEADER_KEY_APP_TOKEN,
        serviceDetails.getFalcon().getAppKey());
    headerProperties.put(X_PROJECT_ID, applicationId);

    Optional<ApplicationIntegration> applicationIntegration = Optional.empty();
    try {
      applicationIntegration = Optional
          .of(restUtils.getRequest(requestUri, headerProperties, ApplicationIntegration.class));
    } catch (Exception e) {
      log.error("Unable to get application integrations for app id: {}", applicationId);
    }

    return applicationIntegration;
  }

  public Optional<NluModel> getNluModelByAppId(String applicationId) {
    String requestUri = String.format(NLU_MODEL_URI_FORMAT, serviceDetails.getFalcon().getUri());

    requestUri = UriComponentsBuilder.fromHttpUrl(requestUri)
        .queryParam("project_id", applicationId).toUriString();

    Properties headerProperties = new Properties();
    headerProperties.put(HttpConstants.HEADER_KEY_APP_NAME,
        serviceDetails.getFalcon().getAppName());
    headerProperties.put(HttpConstants.HEADER_KEY_APP_TOKEN,
        serviceDetails.getFalcon().getAppKey());
    headerProperties.put(X_PROJECT_ID, applicationId);

    Optional<NluModel> nluModel = Optional.empty();
    try {
      Map<String, Object> data = restUtils.getRequest(requestUri, headerProperties, Map.class);
      nluModel = Optional.ofNullable(data).map(im -> {
        NluModel model = new NluModel();
        model.setCqamodelId((String) im.get("id"));
        Map<String, Object> params =
            (Map<String, Object>) ((Map<String, Object>) im.get("nlu_model")).get("params");
        model.setExecutorClassName((String) params.get("executor_class_name"));
        model.setMethodClassName((String) params.get("method_class_name"));
        model.setMethodId((String) params.get("method_id"));
        Map<String, String> modelIds = (Map<String, String>) im.get("model_ids");
        String nluModelId = Optional.ofNullable(modelIds)
            .flatMap(models -> models
                .entrySet().stream().filter(en -> ObjectUtils
                    .nullSafeEquals(DataSetSchema.valueOf(en.getValue()), DataSetSchema.QA_V1))
                .map(Entry::getKey).findFirst())
            .orElse(null);
        if (StringUtils.hasText(nluModelId)) {
          model.setNluModelId(String.format("%s_%s", model.getCqamodelId(), nluModelId));
        }
        return model;
      });
    } catch (Exception e) {
      log.error("Unable to get nlu model for app id: {}", applicationId, e);
    }

    return nluModel;
  }

  public AuthCodeVerifyResponse getAuthCodeValidation(String projectId, Channel channel,
      String userId, String emailId, String code) {
    // TODO: remove channel check after verifying alexa flow
    if (!Channel.AMAZON.equals(channel))
      return new AuthCodeVerifyResponse(null, "disabled", null, null);

    try {
      String url = String.format(AUTH_CODE_VERIFY_URI_FORMAT, serviceDetails.getFalcon().getUri(),
          projectId, userId, channel.getName());

      if (StringUtils.hasText(emailId)) {
        url += "&userEmail=" + emailId;
      }
      if (StringUtils.hasText(code)) {
        url += "&code=" + code;
      }

      Properties headerProperties = new Properties();
      headerProperties.put(HttpConstants.HEADER_KEY_APP_NAME,
          serviceDetails.getFalcon().getAppName());
      headerProperties.put(HttpConstants.HEADER_KEY_APP_TOKEN,
          serviceDetails.getFalcon().getAppKey());
      headerProperties.put(X_PROJECT_ID, projectId);

      return restUtils.getRequest(url, headerProperties, AuthCodeVerifyResponse.class);
    } catch (Exception ex) {
      log.error("Error occurred while validating code given by user", ex);
    }
    return new AuthCodeVerifyResponse(null, "failed", null, "Sorry, something went wrong");
  }


  public AccountLinkingResponse isAmazonAccountLinkingEnabled(String projectId) {
    try {
      String url = String.format(ALEXA_ACCOUNT_LINKING_URI_FORMAT,
          serviceDetails.getFalcon().getUri(), projectId, "DEVELOPMENT");

      Properties headerProperties = new Properties();
      headerProperties.put(HttpConstants.HEADER_KEY_APP_NAME,
          serviceDetails.getFalcon().getAppName());
      headerProperties.put(HttpConstants.HEADER_KEY_APP_TOKEN,
          serviceDetails.getFalcon().getAppKey());
      headerProperties.put(X_PROJECT_ID, projectId);

      return restUtils.getRequest(url, headerProperties, AccountLinkingResponse.class);

    } catch (Exception e) {
      log.error("Error occurred while fetching amazonAccountLinking from falcon", e);
    }
    return null;
  }


  public String fetchProjectIdByAlexaSkillId(String alexaSkillId) {
    String projectId = null;
    try {

      log.debug("Fetching projectId by alexaSkillId from falcon.");

      String requestUri = String.format(PROJECT_ID_BY_ALEXA_SKILL_ID_URI,
          serviceDetails.getFalcon().getUri(), alexaSkillId);
      Properties headerProperties = new Properties();
      headerProperties.put(HttpConstants.HEADER_KEY_APP_NAME,
          serviceDetails.getFalcon().getAppName());
      headerProperties.put(HttpConstants.HEADER_KEY_APP_TOKEN,
          serviceDetails.getFalcon().getAppKey());

      String response = restUtils.getRequest(requestUri, headerProperties, String.class);
      Map<String, String> responseMap =
          objectMapper.readValue(response, new TypeReference<Map<String, String>>() {});
      projectId = responseMap.get("project_id");
      log.debug("Successfully fetched projectId by alexa skillId from falcon.");
    } catch (Exception ex) {
      log.error("Error occurred while fetching projectId from falcon {}", ex);
    }
    return projectId;
  }


  public String getBaseUri() {
    return serviceDetails.getFalcon().getUri();
  }

  public String getRandomAuthCode(String projectId, String userId) {
    Validate.notBlank(projectId);
    Validate.notBlank(userId);

    log.debug("Fetching auth code for projectId {} and userId {}", projectId, userId);
    String response = null;
    try {
      String url = String.format(AUTH_CODE_URI_FORMAT, serviceDetails.getFalcon().getUri(),
          projectId, userId);
      Properties headerProperties = new Properties();
      headerProperties.put(HttpConstants.HEADER_KEY_APP_NAME,
          serviceDetails.getFalcon().getAppName());
      headerProperties.put(HttpConstants.HEADER_KEY_APP_TOKEN,
          serviceDetails.getFalcon().getAppKey());
      headerProperties.put(X_PROJECT_ID, projectId);

      response = restUtils.getRequest(url, headerProperties, String.class);
    } catch (Exception e) {
      log.error("Error occurred while fetching auth code from falcon{}", e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
    if (!StringUtils.hasLength(response)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No code available");
    }
    return response;
  }

  public void deleteAuthCode(String projectId, String userId) {
    Validate.notBlank(projectId);
    Validate.notBlank(userId);

    log.debug("Deleting auth code for projectId {} and userId {}", projectId, userId);
    try {
      String url = String.format(AUTH_CODE_URI_FORMAT, serviceDetails.getFalcon().getUri(),
          projectId, userId);
      Properties headerProperties = new Properties();
      headerProperties.put(HttpConstants.HEADER_KEY_APP_NAME,
          serviceDetails.getFalcon().getAppName());
      headerProperties.put(HttpConstants.HEADER_KEY_APP_TOKEN,
          serviceDetails.getFalcon().getAppKey());
      headerProperties.put(X_PROJECT_ID, projectId);

      restUtils.deleteRequest(url, headerProperties, Void.class);
    } catch (Exception e) {
      log.error("Error occurred while deleting codes from falcon", e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
    }
  }

  public List<DataSet> getDataSets(String applicationId, DataSetSchema schema, String query) {
    StringBuilder uriBuf = new StringBuilder(String.format(DATASET_URI_FORMAT,
        serviceDetails.getFalcon().getUri(), applicationId, schema));

    if (StringUtils.hasText(query)) {
      uriBuf.append("&query=").append(query);
    }

    Properties headerProperties = new Properties();
    headerProperties.put(X_APP_NAME, serviceDetails.getFalcon().getAppName());
    headerProperties.put(X_APP_TOKEN, serviceDetails.getFalcon().getAppKey());
    headerProperties.put(X_PROJECT_ID, applicationId);

    try {
      String dataSnapshots =
          restUtils.getRequest(uriBuf.toString(), headerProperties, String.class);
      return objectMapper.readValue(dataSnapshots, new TypeReference<List<DataSet>>() {});
    } catch (Exception e) {
      log.error("Unable to get conversation control details", e);
      return Lists.newArrayList();
    }
  }

  public Tenant getTenantByProjectId(String applicationId) {
    StringBuilder uriBuf = new StringBuilder(String.format(TENANT_BY_PROJECT_ID_URI_FORMAT,
        serviceDetails.getFalcon().getUri(), applicationId));

    Properties headerProperties = new Properties();
    headerProperties.put(X_APP_NAME, serviceDetails.getFalcon().getAppName());
    headerProperties.put(X_APP_TOKEN, serviceDetails.getFalcon().getAppKey());
    headerProperties.put(X_PROJECT_ID, applicationId);

    try {
      String tenant = restUtils.getRequest(uriBuf.toString(), headerProperties, String.class);
      return objectMapper.readValue(tenant, Tenant.class);
    } catch (Exception e) {
      log.error("Unable to get tenant by project id {}", applicationId);
      return new Tenant();
    }
  }

  public Project getProjectById(String applicationId) {
    StringBuilder uriBuf = new StringBuilder(String.format(PROJECT_BY_ID_URI_FORMAT,
        serviceDetails.getFalcon().getUri(), applicationId));

    Properties headerProperties = new Properties();
    headerProperties.put(X_APP_NAME, serviceDetails.getFalcon().getAppName());
    headerProperties.put(X_APP_TOKEN, serviceDetails.getFalcon().getAppKey());
    headerProperties.put(X_PROJECT_ID, applicationId);

    try {
      String project = restUtils.getRequest(uriBuf.toString(), headerProperties, String.class);
      return objectMapper.readValue(project, Project.class);
    } catch (Exception e) {
      log.error("Unable to get project by id {}", applicationId);
      return new Project();
    }
  }

  public TenantCloneResponse cloneTenant(String tenantId, TenantCloneRequest request) {
    StringBuilder uriBuf = new StringBuilder(
        String.format(TENANT_CLONE_URI_FORMAT, serviceDetails.getFalcon().getUri(), tenantId));

    Properties headerProperties = new Properties();
    headerProperties.put(X_APP_NAME, serviceDetails.getFalcon().getAppName());
    headerProperties.put(X_APP_TOKEN, serviceDetails.getFalcon().getAppKey());

    try {
      TenantCloneResponse response = restUtils.postRequest(uriBuf.toString(),
          objectMapper.writeValueAsString(request), headerProperties, TenantCloneResponse.class);
      response.setStatus(new Status(201, "cloned", "successfully cloned tenant"));
      return response;
    } catch (Exception e) {
      TenantCloneResponse response = new TenantCloneResponse();
      response.setStatus(new Status(500, "failed", "unable to clone tenant"));
      return response;
    }
  }

  public ProjectCloneResponse cloneProjects(String tenantId, ProjectCloneRequest request) {
    StringBuilder uriBuf = new StringBuilder(
        String.format(PROJECT_CLONE_URI_FORMAT, serviceDetails.getFalcon().getUri(), tenantId));

    Properties headerProperties = new Properties();
    headerProperties.put(X_APP_NAME, serviceDetails.getFalcon().getAppName());
    headerProperties.put(X_APP_TOKEN, serviceDetails.getFalcon().getAppKey());

    try {
      ProjectCloneResponse response = restUtils.postRequest(uriBuf.toString(),
          objectMapper.writeValueAsString(request.getCloneApplicationIds()), headerProperties,
          ProjectCloneResponse.class);
      response.setStatus(new Status(201, "cloned", "successfully cloned projects"));
      return response;
    } catch (Exception e) {
      ProjectCloneResponse response = new ProjectCloneResponse();
      response.setStatus(new Status(500, "failed", "unable to clone projects"));
      return response;
    }
  }

  public BookingWidgetConfig getBookingWidgetConfig(String widgetId) {
    StringBuilder uriBuf = new StringBuilder(String.format(WIDGET_CONFIG_BY_ID_URI_FORMAT,
        serviceDetails.getFalcon().getUri(), widgetId));

    Properties headerProperties = new Properties();
    headerProperties.put(X_APP_NAME, serviceDetails.getFalcon().getAppName());
    headerProperties.put(X_APP_TOKEN, serviceDetails.getFalcon().getAppKey());

    try {
      String config = restUtils.getRequest(uriBuf.toString(), headerProperties, String.class);
      return objectMapper.readValue(config, BookingWidgetConfig.class);
    } catch (Exception e) {
      log.error("Unable to get widget config by id {}", widgetId);
      return new BookingWidgetConfig();
    }
  }
}
