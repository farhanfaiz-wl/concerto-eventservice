package ai.concerto.event.handler.request;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.ClientType;
import ai.concerto.event.enums.Source;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.exception.RequestHandlerException;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.ServiceProvider;
import ai.concerto.event.exchange.ApplicationIntegration.WhatsappIntegration;
import ai.concerto.event.exchange.WhatsappUserRequest;
import ai.concerto.event.handler.UserRequestHandler;
import ai.concerto.event.service.IntegrationService;
import ai.concerto.event.utils.PhoneUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WhatsappRequestHandler implements UserRequestHandler {

  private static final String TRAIL_ACCOUNT_MESSAGE = "Sent from your Twilio trial account - ";
  private static final String WHATSAPP_PREFIX = "whatsapp:";

  @Autowired
  private IntegrationService integrationService;

  @Autowired
  private ObjectMapper snakeCaseMapper;

  @SneakyThrows
  @Override
  public DERequest getDeRequest(String applicationId, Object request) {
    throw new RequestHandlerException("WhatsApp integration requires a vendor");
  }

  protected DERequest getDeRequest(String applicationId, Vendor vendor) {
    ApplicationIntegration integration =
        integrationService.getApplicationIntegration(applicationId);
    WhatsappIntegration channelIntegration = (WhatsappIntegration) integrationService
        .getChannelIntegration(applicationId, Channel.WHATSAPP, integration);

    if (ObjectUtils.isEmpty(channelIntegration))
      throw new RequestHandlerException("WhatsApp channel is disabled!");

    Optional<ServiceProvider> serviceProvider =
        channelIntegration.getServiceProvidersList().stream()
            .filter(provider -> vendor.equals(provider.getMessageServiceProvider())).findFirst();

    if (!serviceProvider.isPresent())
      throw new RequestHandlerException(
          String.format("%s provider not found for whatsapp integration!", vendor.name()));

    DERequest deRequest = new DERequest();
    deRequest.setProjectId(applicationId);
    deRequest.setClientType(ClientType.MESSAGING.getName());
    deRequest.setSource(Source.whatsapp);
    deRequest = integrationService.updateIntegrationDetails(deRequest, integration);
    deRequest.setQAEnable(channelIntegration.getEnableQA());
    deRequest.setSendSearchResults(channelIntegration.getShowSearchResult());
    deRequest.setSessionTimeOut(channelIntegration.getSessionTimeout());
    deRequest.setSessionTimeoutPrompt(channelIntegration.getSessionTimeoutPrompt());
    deRequest.setBotAuthId(serviceProvider.get().getProviderDetails().getAccountId());
    deRequest.setBotAuthToken(serviceProvider.get().getProviderDetails().getAuthToken());
    deRequest.setAccessToken(serviceProvider.get().getProviderDetails().getAccessToken());
    deRequest.setBotPhoneNumber(channelIntegration.getPhoneNumber());

    return deRequest;
  }

  protected void processWhatsappUserRequest(DERequest deRequest, Object userRequest)
      throws NumberParseException {
    WhatsappUserRequest whatsappRequest =
        snakeCaseMapper.convertValue(userRequest, new TypeReference<WhatsappUserRequest>() {});

    String waId = whatsappRequest.getContacts().get(0).getWaId();
    if (waId.startsWith(WHATSAPP_PREFIX)) {
      waId = waId.replace(WHATSAPP_PREFIX, "");
    }
    String userPhoneNumber = PhoneUtils.standardizedPhoneNumber(waId.trim());
    String userLastMessage = whatsappRequest.getMessages().get(0).getText().getBody();
    if (userLastMessage.contains(TRAIL_ACCOUNT_MESSAGE)) {
      userLastMessage = userLastMessage.replace(TRAIL_ACCOUNT_MESSAGE, "").trim();
    }
    if (userLastMessage.length() == 2 && Character.isDigit(userLastMessage.charAt(0))
        && userLastMessage.contains(".")) {
      userLastMessage = userLastMessage.replace(".", "").trim();
    }
    PhoneNumber phoneNumber = PhoneUtils.getPhoneNumber(userPhoneNumber);
    deRequest.setPhoneNumber(String.valueOf(phoneNumber.getNationalNumber()));
    deRequest.setPhoneNumberCountryCode(String.valueOf(phoneNumber.getCountryCode()));
    deRequest.setPhoneNumberWithCountryCode(userPhoneNumber);
    deRequest.setUserId(userPhoneNumber);
    deRequest.setUserFirstName(whatsappRequest.getContacts().get(0).getProfile().getName());
    deRequest.setUserInputLast(userLastMessage);
  }
}
