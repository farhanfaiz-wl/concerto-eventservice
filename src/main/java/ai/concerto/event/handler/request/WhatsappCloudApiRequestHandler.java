package ai.concerto.event.handler.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.exchange.WhatsappCloudApiRequest;
import ai.concerto.event.utils.PhoneUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WhatsappCloudApiRequestHandler extends WhatsappRequestHandler {

  @Autowired
  private ObjectMapper snakeCaseMapper;

  @SneakyThrows
  @Override
  public DERequest getDeRequest(String applicationId, Object request) {
    DERequest deRequest = getDeRequest(applicationId, Vendor.CLOUD_API);

    snakeCaseMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    WhatsappCloudApiRequest whatsappCloudApiRequest =
        snakeCaseMapper.convertValue(request, new TypeReference<WhatsappCloudApiRequest>() {});

    String from = whatsappCloudApiRequest.getMessages().get(0).getFrom();

    String userPhoneNumber = PhoneUtils.standardizedPhoneNumber(from.trim());
    String userLastMessage = null;
    if (whatsappCloudApiRequest.getMessages().get(0).getType().equals("text")) {
      userLastMessage = whatsappCloudApiRequest.getMessages().get(0).getText().getBody();
    } else if (whatsappCloudApiRequest.getMessages().get(0).getInteractive().getType()
        .equals("button_reply")) {
      userLastMessage =
          whatsappCloudApiRequest.getMessages().get(0).getInteractive().getButtonReply().getTitle();
    } else {
      userLastMessage = whatsappCloudApiRequest.getMessages().get(0).getInteractive().getListReply()
          .getDescription();
    }

    PhoneNumber phoneNumber = PhoneUtils.getPhoneNumber(userPhoneNumber);
    deRequest.setPhoneNumber(String.valueOf(phoneNumber.getNationalNumber()));
    deRequest.setPhoneNumberCountryCode(String.valueOf(phoneNumber.getCountryCode()));
    deRequest.setPhoneNumberWithCountryCode(from);
    deRequest.setUserId(from);
    deRequest.setUserFirstName(whatsappCloudApiRequest.getContacts().get(0).getProfile().getName());
    deRequest.setUserInputLast(userLastMessage);
    deRequest.setPhoneNumberId(whatsappCloudApiRequest.getMetadata().getPhoneNumberId());

    return deRequest;
  }

}
