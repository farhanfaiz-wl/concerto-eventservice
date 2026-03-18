package ai.concerto.event.handler.request;

import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.ClientType;
import ai.concerto.event.enums.Source;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.MultiAppDetails;
import ai.concerto.event.handler.UserRequestHandler;
import ai.concerto.event.service.IntegrationService;
import ai.concerto.event.service.MultiAppService;
import ai.concerto.event.utils.PhoneUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.Twilio;
import com.twilio.rest.lookups.v2.PhoneNumber;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class TelephonyTwilioRequestHandler implements UserRequestHandler {

  @Autowired
  private IntegrationService integrationService;

  @Autowired
  private MultiAppService multiAppService;

  @Autowired
  private ObjectMapper objectMapper;

  @Value("${twilio.accountSid}")
  private String accountSid;

  @Value("${twilio.authToken}")
  private String authToken;

  private static final Map<String, String> optionMapping = Stream
      .of(new String[][] {{"zero", "0"}, {"one", "1"}, {"two", "2"}, {"three", "3"}, {"four", "4"},
          {"five", "5"}, {"six", "6"}, {"seven", "7"}, {"eight", "8"}, {"nine", "9"}, {"ten", "10"},
          {"bee", "b"}, {"been", "b"}, {"see", "c"}, {"sea", "c"}, {"seen", "c"}, {"dee", "d"},
          {"deer", "d"}, {"gene", "g"}, {"etch", "h"}, {"hedge", "h"}, {"eye", "i"}, {"aye", "i"},
          {"jay", "j"}, {"kay", "k"}, {"el", "l"}, {"em", "m"}, {"en", "n"}, {"oh", "o"},
          {"pea", "p"}, {"pee", "p"}, {"queue", "q"}, {"are", "r"}, {"yes", "s"}, {"es", "s"},
          {"tee", "t"}, {"tea", "t"}, {"you", "u"}, {"vee", "v"}, {"we", "v"}, {"wee", "v"},
          {"ex", "x"}, {"hex", "x"}, {"why", "y"}, {"zed", "z"}, {"even", "7"}, {"evan", "7"},
          {"fi", "5"}, {"for", "4"}, {"tree", "3"}, {"to", "2"}})
      .collect(Collectors.toMap(data -> data[0], data -> data[1]));
  private static final String TRAIL_ACCOUNT_MESSAGE = "Sent from your Twilio trial account - ";
  private static final String FROM = "From";
  private static final String VOICE = "voice_";
  private static final String TWILIO_SILENCE_MESSAGE = "twilio_silence_intent";

  @Override
  @SneakyThrows
  public DERequest getDeRequest(String applicationId, Object request) {
    Map<String, String> params =
        objectMapper.convertValue(request, new TypeReference<Map<String, String>>() {});

    DERequest deRequest = new DERequest();
    String fromNumber = params.get(FROM);
    String callStatus = params.get("CallStatus");
    deRequest.setTurnId(UUID.randomUUID().toString());
    String userId = VOICE.concat(fromNumber);
    deRequest.setUserId(userId);
    com.google.i18n.phonenumbers.Phonenumber.PhoneNumber phoneNumber =
        PhoneUtils.getPhoneNumber(fromNumber);

    Twilio.init(accountSid, authToken);
    PhoneNumber twilioPhoneNumber =
        PhoneNumber.fetcher(fromNumber).setFields("line_type_intelligence").fetch();
    if (!ObjectUtils.isEmpty(twilioPhoneNumber.getLineTypeIntelligence())) {
      deRequest.setPhoneNumberLineType(
          twilioPhoneNumber.getLineTypeIntelligence().get("type").toString());
    }

    // format the phone number
    deRequest.setPhoneNumberWithCountryCode(fromNumber);
    deRequest.setPhoneNumberCountryCode(String.valueOf(phoneNumber.getCountryCode()));
    deRequest.setPhoneNumber(String.valueOf(phoneNumber.getNationalNumber()));
    // get welcome prompt for ringing call status
    if ("ringing".equals(callStatus)) {
      multiAppService.clearMultiApp(applicationId, userId, Channel.TELEPHONY);
      deRequest.setUserInputLast("");
      deRequest.setSessionNew(true);
    } else {
      // set original user input
      String userLastMessage = params.get("SpeechResult");
      if (StringUtils.hasText(userLastMessage)) {
        if (userLastMessage.contains(TRAIL_ACCOUNT_MESSAGE)) {
          userLastMessage = userLastMessage.replace(TRAIL_ACCOUNT_MESSAGE, "");
          userLastMessage = userLastMessage.trim();
        }
        if (optionMapping.containsKey(userLastMessage)) {
          userLastMessage = optionMapping.get(userLastMessage);
        }
        if (userLastMessage.endsWith(".")) {
          // remove the last character
          userLastMessage = userLastMessage.substring(0, userLastMessage.length() - 1);
        }
      } else {
        userLastMessage = TWILIO_SILENCE_MESSAGE;
      }
      deRequest.setUserInputLast(userLastMessage);
    }

    // check for multi application
    MultiAppDetails multiAppDetails =
        multiAppService.checkMultiApp(applicationId, deRequest, Channel.TELEPHONY);
    if (!ObjectUtils.isEmpty(multiAppDetails)) {
      applicationId = multiAppDetails.getChangedAppId();
      // set user input last for new session only
      if (multiAppDetails.isSessionNew()) {
        deRequest.setUserInputLast("");
      }
      multiAppDetails.setSessionNew(false);
      // set original application id as Key id to identify next onward call
      multiAppDetails.setKeyAppId(multiAppDetails.getOriginalAppId());
      multiAppService.setMultiApp(multiAppDetails, 1800);
    }

    deRequest.setProjectId(applicationId);
    ApplicationIntegration integration =
        integrationService.getApplicationIntegration(applicationId);
    deRequest = integrationService.updateIntegrationDetails(deRequest, integration);
    deRequest.setSource(Source.telephony);
    deRequest.setClientType(ClientType.VOICE.getName());
    deRequest.setSendSearchResults(Boolean.FALSE);
    deRequest.setQAEnable(Boolean.TRUE);
    deRequest.setSessionTimeOut(1800);

    return deRequest;
  }

}
