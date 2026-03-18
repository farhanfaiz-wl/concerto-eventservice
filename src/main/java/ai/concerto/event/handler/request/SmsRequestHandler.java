package ai.concerto.event.handler.request;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.ClientType;
import ai.concerto.event.enums.Source;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.exception.RequestHandlerException;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.ServiceProvider;
import ai.concerto.event.exchange.ApplicationIntegration.SmsIntegration;
import ai.concerto.event.exchange.MultiAppDetails;
import ai.concerto.event.exchange.SmsUserRequest;
import ai.concerto.event.handler.UserRequestHandler;
import ai.concerto.event.service.IntegrationService;
import ai.concerto.event.service.MultiAppService;
import ai.concerto.event.utils.PhoneUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SmsRequestHandler implements UserRequestHandler {

  private static final String TRAIL_ACCOUNT_MESSAGE = "Sent from your Twilio trial account - ";

  @Autowired
  private IntegrationService integrationService;

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private MultiAppService multiAppService;

  @SneakyThrows
  @Override
  public DERequest getDeRequest(String applicationId, Object request) {
    throw new RequestHandlerException("Sms integration requires a vendor");
  }

  protected void setIntegrationdetails(DERequest deRequest, String applicationId, Vendor vendor,
      MultiAppDetails multiAppDetails) {
    String projectId = applicationId;
    if (!ObjectUtils.isEmpty(multiAppDetails) && multiAppDetails.isSessionNew()) {
      projectId = multiAppDetails.getOriginalAppId();
    }

    ApplicationIntegration integration = integrationService.getApplicationIntegration(projectId);
    SmsIntegration channelIntegration = (SmsIntegration) integrationService
        .getChannelIntegration(applicationId, Channel.SMS, integration);

    if (ObjectUtils.isEmpty(channelIntegration))
      throw new RequestHandlerException("Sms channel is disabled!");

    Optional<ServiceProvider> serviceProvider =
        channelIntegration.getServiceProvidersList().stream()
            .filter(provider -> vendor.equals(provider.getMessageServiceProvider())).findFirst();

    if (!serviceProvider.isPresent())
      throw new RequestHandlerException(
          String.format("%s provider not found for sms integration!", vendor.name()));

    if (!ObjectUtils.isEmpty(multiAppDetails)) {
      deRequest.setProjectId(multiAppDetails.getChangedAppId());
      ApplicationIntegration changedAppIntegration =
          integrationService.getApplicationIntegration(multiAppDetails.getChangedAppId());
      deRequest = integrationService.updateIntegrationDetails(deRequest, changedAppIntegration);

      // update the multiapp details only in case of new session
      if (multiAppDetails.isSessionNew()) {
        deRequest.setUserInputLast("");
      }
      multiAppDetails.setSessionNew(false);
      // set original application id as Key id to identify next onward call
      multiAppDetails.setKeyAppId(multiAppDetails.getOriginalAppId());
      multiAppService.setMultiApp(multiAppDetails, deRequest.getSessionTimeOut());

    } else {
      deRequest.setProjectId(projectId);
      deRequest = integrationService.updateIntegrationDetails(deRequest, integration);
    }

    deRequest.setClientType(ClientType.MESSAGING.getName());
    deRequest.setSource(Source.sms);
    deRequest.setQAEnable(channelIntegration.getEnableQA());
    deRequest.setSendSearchResults(channelIntegration.getShowSearchResult());
    deRequest.setSessionTimeOut(channelIntegration.getSessionTimeout());
    deRequest.setSessionTimeoutPrompt(channelIntegration.getSessionTimeoutPrompt());
    deRequest.setBotAuthId(serviceProvider.get().getProviderDetails().getAccountId());
    deRequest.setBotAuthToken(serviceProvider.get().getProviderDetails().getAuthToken());
    deRequest.setBotPhoneNumber(channelIntegration.getPhoneNumber());
  }


  @SneakyThrows
  protected DERequest getDeRequest(Object userRequest) {

    SmsUserRequest smsUserRequest = mapper.convertValue(userRequest, SmsUserRequest.class);
    String userPhoneNumber = PhoneUtils.standardizedPhoneNumber(smsUserRequest.getFrom());
    String userLastMessage = smsUserRequest.getText();
    if (userLastMessage.contains(TRAIL_ACCOUNT_MESSAGE)) {
      userLastMessage = userLastMessage.replace(TRAIL_ACCOUNT_MESSAGE, "").trim();
    }
    if (userLastMessage.length() == 2 && Character.isDigit(userLastMessage.charAt(0))
        && userLastMessage.contains(".")) {
      userLastMessage = userLastMessage.replace(".", "").trim();
    }
    PhoneNumber phoneNumber = PhoneUtils.getPhoneNumber(userPhoneNumber);
    DERequest deRequest = new DERequest();
    deRequest.setPhoneNumber(String.valueOf(phoneNumber.getNationalNumber()));
    deRequest.setPhoneNumberCountryCode(String.valueOf(phoneNumber.getCountryCode()));
    deRequest.setPhoneNumberWithCountryCode(smsUserRequest.getFrom());
    deRequest.setUserId(smsUserRequest.getFrom());
    deRequest.setUserFirstName(smsUserRequest.getName());
    deRequest.setUserInputLast(userLastMessage);

    return deRequest;

  }
}

