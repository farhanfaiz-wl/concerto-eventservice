package ai.concerto.event.service;

import ai.concerto.event.dto.DERequest;
import ai.concerto.event.dto.DESettings;
import ai.concerto.event.dto.NluModel;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.exception.RequestHandlerException;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.ChannelIntegration;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Slf4j
@Service
public class IntegrationService {

  @Autowired
  private FalconService falconService;

  public DERequest updateIntegrationDetails(DERequest deRequest,
      ApplicationIntegration integration) {

    deRequest.setDeSettins(getDESettings(integration));
    deRequest.setTemplate(integration.getTemplate());
    deRequest.setTenantId(integration.getTenantId());

    if (ObjectUtils.isEmpty(deRequest.getDeSettins()))
      deRequest.setDeSettins(new DESettings());

    Optional.ofNullable(integration.getProjectSettings()).ifPresent(projectSetting -> {
      deRequest.setLanguage(
          Optional.ofNullable(projectSetting.get("project_lang")).map(Object::toString).orElse(""));
      deRequest.setCenterId(
          Optional.ofNullable(projectSetting.get("center_id")).map(Object::toString).orElse(""));
      deRequest.setLlmEnabled(Optional.ofNullable(projectSetting.get("llm_enabled"))
          .map(obj -> Boolean.valueOf(obj.toString())).orElse(false));

    });

    if (!ObjectUtils.isEmpty(integration.getProjectSettings().get("compliance_type")))
      deRequest.setComplianceType((String) integration.getProjectSettings().get("compliance_type"));

    Optional.ofNullable(getCqaModelIdByAppId(deRequest.getProjectId()))
        .ifPresent(deRequest::setCqamodelId);

    return deRequest;
  }

  @SneakyThrows
  public ApplicationIntegration getApplicationIntegration(String applicationId) {
    Optional<ApplicationIntegration> integration =
        falconService.getApplicationIntegration(applicationId);

    if (!integration.isPresent()) {
      String errMsg = String.format("No integration was found for project: %s", applicationId);
      log.error(errMsg);
      throw new RequestHandlerException(
          String.format("No Integration found for project %s", applicationId));
    }

    return integration.get();
  }

  @SneakyThrows
  public ChannelIntegration getChannelIntegration(String applicationId, Channel channel,
      ApplicationIntegration integration) {

    return switch (channel) {
      case HTML5, CHATBOT -> integration.getChatbotIntegration();
      case GOOGLE_BUSINESS_MESSAGE -> integration.getGoogleBusinessIntegration();
      case WHATSAPP -> integration.getWhatsappIntegration();
      case SLACK -> integration.getSlackIntegration();
      case SMS -> integration.getSmsIntegration();
      case EMAIL -> integration.getEmailIntegration();
      case AMAZON -> integration.getAmazonIntegration();
      case FACEBOOK -> integration.getFacebookIntegration();
      case WEBSITE -> integration.getWebsiteIntegration();
      case WIDGET -> integration.getWidgetIntegration();
      case BOOKING_WIDGET -> integration.getBookingWidgetIntegration();
      default -> {
        String errMsg = String.format("Unknown channel `%s`", channel);
        log.error(errMsg);
        throw new RequestHandlerException(errMsg);
      }
    };
  }



  public String getCqaModelIdByAppId(String applicationId) {
    Optional<NluModel> nluModel = falconService.getNluModelByAppId(applicationId);
    if (nluModel.isPresent()) {
      return nluModel.get().getCqamodelId();
    }

    return null;
  }

  public DESettings getDESettings(ApplicationIntegration integration) {
    DESettings deSettings = new DESettings();
    if (ObjectUtils.isEmpty(integration.getProjectSettings()))
      return deSettings;

    if (integration.getProjectSettings().containsKey("context_switching"))
      deSettings
          .setContextSwitching((Boolean) integration.getProjectSettings().get("context_switching"));

    if (integration.getProjectSettings().containsKey("use_chitchat"))
      deSettings.setUseChitchat((Boolean) integration.getProjectSettings().get("use_chitchat"));

    if (integration.getProjectSettings().containsKey("use_long_answer"))
      deSettings
          .setUseLongAnswer((Boolean) integration.getProjectSettings().get("use_long_answer"));

    if (integration.getProjectSettings().containsKey("live_agent"))
      deSettings.setLiveAgent((Boolean) integration.getProjectSettings().get("live_agent"));

    if (integration.getProjectSettings().containsKey("disable_recommendations"))
      deSettings.setDisableRecommendations(
          (Boolean) integration.getProjectSettings().get("disable_recommendations"));

    if (integration.getProjectSettings().containsKey("disable_qa_on_message"))
      deSettings.setDisableQaOnMessage(
          (Boolean) integration.getProjectSettings().get("disable_qa_on_message"));

    if (integration.getProjectSettings().containsKey("context_switching_message"))
      deSettings.setContextSwitchingMessage(
          (Boolean) integration.getProjectSettings().get("context_switching_message"));

    if (integration.getProjectSettings().containsKey("select_correct_appoint_msg"))
      deSettings.setSelectCorrectAppointMsg(
          (String) integration.getProjectSettings().get("select_correct_appoint_msg"));

    return deSettings;
  }
}
