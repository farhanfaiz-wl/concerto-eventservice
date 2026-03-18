package ai.concerto.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.config.ServiceDetails.Service;
import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.exchange.BotTelephonyTwilioResponse;
import ai.concerto.event.handler.request.TelephonyTwilioRequestHandler;
import ai.concerto.event.handler.request.TelephonyVonageRequestHandler;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.render.TelephonyTwilioRenderer;
import ai.concerto.event.render.TelephonyVonageRenderer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.twiml.voice.Say;
import com.vonage.client.incoming.InputEvent;
import com.vonage.client.voice.TextToSpeechLanguage;
import com.vonage.client.voice.ncco.SpeechSettings;
import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class TelephonyServiceTest {
  @InjectMocks
  @Resource
  public TelephonyService telephonyService;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private ServiceDetails serviceDetails;

  @Mock
  private TelephonyTwilioRequestHandler telephonyTwilioRequestHandler;

  @Mock
  private TelephonyVonageRequestHandler telephonyVonageRequestHandler;

  @Mock
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Mock
  private RedisTemplate redisTemplate;


  @Mock
  private ValueOperations valueOperations;

  @Mock
  private HashOperations hashOperations;

  @Mock
  private SessionService sessionService;

  @Mock
  private UserRequestService userRequestService;

  @Mock
  private DEService deService;

  @Mock
  private TelephonyTwilioRenderer telephonyTwilioRenderer;

  @Mock
  private TelephonyVonageRenderer telephonyVonageRenderer;

  @BeforeEach
  public void setUp() {

    MockitoAnnotations.openMocks(this);
  }

  @Test
  void processTwilioVoiceRequestErrorTest() throws Exception {
    MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
    map.set("CallStatus", "active");
    map.add("SpeechResult", "result");
    map.add("language", "English");
    map.add("ntts", "ntts");
    map.add("bargeIn", "true");
    map.add("speechModel", "phone_call");
    URI uri = URI.create(
        "https://someUri/event/twilio/voice/someApplicationId?language=English&ntts=ntts&bargeIn=true&speechModel=phone_call");
    map.add("uri", uri.toString());

    Map<String, String> request = map.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));

    Service service = new ServiceDetails().getEventservice();
    service.setUri("https://someUri");
    when(serviceDetails.getEventservice()).thenReturn(service);

    when(userRequestService.processUserRequest("someApplicationId", request, Channel.TELEPHONY,
        Vendor.TWILIO.name())).thenThrow(new Exception());

    assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Say voice=\"Polly.Joanna-Neural\">Sorry something went wrong</Say><Hangup/></Response>",
        telephonyService.processTwilioVoiceRequest("someApplicationId", "English", "ntts", "60", "",
            true, false, "phone_call", map));
  }

  @Test
  void processTwilioVoiceRequestWithSpeechResultTest() throws Exception {
    MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
    map.set("CallStatus", "active");
    map.add("SpeechResult", "result");
    map.add("language", "English");
    map.add("ntts", "ntts");
    map.add("bargeIn", "true");
    map.add("speechModel", "phone_call");
    URI uri = URI.create(
        "https://someUri/event/twilio/voice/someApplicationId?language=English&ntts=ntts&bargeIn=true&speechModel=phone_call");
    map.add("uri", uri.toString());

    Map<String, String> request = map.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));

    Service service = new ServiceDetails().getEventservice();
    service.setUri("https://someUri");
    when(serviceDetails.getEventservice()).thenReturn(service);

    BotTelephonyTwilioResponse botTelephonyTwilioResponse = new BotTelephonyTwilioResponse();
    botTelephonyTwilioResponse.setXmlResponse(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Say voice=\"Polly.Joanna-Neural\">How are you</Say></Response>");

    when(userRequestService.processUserRequest("someApplicationId", request, Channel.TELEPHONY,
        Vendor.TWILIO.name())).thenReturn(botTelephonyTwilioResponse);

    assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Say voice=\"Polly.Joanna-Neural\">How are you</Say></Response>",
        telephonyService.processTwilioVoiceRequest("someApplicationId", "English", "ntts", "", "",
            true, false, "phone_call", map));
  }

  @Test
  void setVoiceTest() {
    String ntts = Say.Voice.MAN.toString();
    assertEquals(Say.Voice.MAN.toString(), ntts);
  }

  @Test
  void processVonageIncomingCallTest() throws Exception {
    ServiceDetails.Service service = new ServiceDetails.Service();
    service.setUri("someUri");
    when(serviceDetails.getEventservice()).thenReturn(service);
    InputEvent inputEvent = new InputEvent();
    DERequest deRequest = new DERequest();
    DEBotResponse deResponse = new DEBotResponse();
    when(telephonyVonageRequestHandler.getDeRequest("applicationId", inputEvent))
        .thenReturn(deRequest);
    when(deService.postDeRequest(deRequest)).thenReturn(deResponse);
    Mockito.doReturn(null).when(telephonyVonageRenderer).render(deRequest, deResponse, "en-US", 4,
        5, 1, new StringBuilder("someUri"));
    assertNull(
        telephonyService.processVonageIncomingCall("applicationId", "", null, null, 1, inputEvent));

  }

  @Test
  void getTextToSpeechLanguageAmericanEnglishTest() {
    TextToSpeechLanguage language = TextToSpeechLanguage.AMERICAN_ENGLISH;
    assertEquals(telephonyService.getTextSpeachLanguage("AMERICAN_ENGLISH"), language);
  }

  @Test
  void getTextToSpeechLanguageAmericanSpanishTest() {
    TextToSpeechLanguage language = TextToSpeechLanguage.AMERICAN_SPANISH;
    assertEquals(language, telephonyService.getTextSpeachLanguage("AMERICAN_SPANISH"));
  }

  @Test
  void getTextToSpeechLanguageIndianEnglishTest() {
    TextToSpeechLanguage language = TextToSpeechLanguage.INDIAN_ENGLISH;
    assertEquals(language, telephonyService.getTextSpeachLanguage("INDIAN_ENGLISH"));
  }

  @Test
  void getTextToSpeechDefaultLanguageTest() {
    TextToSpeechLanguage language = TextToSpeechLanguage.AMERICAN_ENGLISH;
    assertEquals(language, telephonyService.getTextSpeachLanguage("en-ZA"));
  }

  @Test
  void getSpeechLanguageEnglishUnitedStatesTest() {
    String englishUnitedStates = "en-US";
    assertEquals(com.vonage.client.voice.ncco.SpeechSettings.Language.ENGLISH_UNITED_STATES,
        telephonyService.getSpeachLanguage(englishUnitedStates));
  }

  @Test
  void getSpeechLanguageSpanishUnitedStatesTest() {
    com.vonage.client.voice.ncco.SpeechSettings.Language spanishUnitedStates =
        SpeechSettings.Language.SPANISH_UNITED_STATES;
    assertEquals(spanishUnitedStates, telephonyService.getSpeachLanguage("SPANISH_UNITED_STATES"));
  }

  @Test
  void getSpeechLanguageEnglishIndiaTest() {
    com.vonage.client.voice.ncco.SpeechSettings.Language englishIndia =
        SpeechSettings.Language.ENGLISH_INDIA;
    assertEquals(englishIndia, telephonyService.getSpeachLanguage("ENGLISH_INDIA"));
  }

  @Test
  void getSpeechLanguageDefaultTest() {
    com.vonage.client.voice.ncco.SpeechSettings.Language unitedStatesLanguage =
        SpeechSettings.Language.ENGLISH_UNITED_STATES;
    assertEquals(unitedStatesLanguage, telephonyService.getSpeachLanguage("SPANISH_CHILE"));
  }

}
