package ai.concerto.event.service;

import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.dto.Session;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.Source;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.exchange.BotResponse;
import ai.concerto.event.exchange.BotTelephonyTwilioResponse;
import ai.concerto.event.exchange.Tenant;
import ai.concerto.event.exchange.UserDetails;
import ai.concerto.event.handler.request.TelephonyOzonetelRequestHandler;
import ai.concerto.event.handler.request.TelephonyVonageRequestHandler;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.render.TelephonyOzonetelRenderer;
import ai.concerto.event.render.TelephonyVonageRenderer;
import ai.concerto.event.utils.Constants;
import ai.concerto.event.utils.PhoneUtils;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Gather.Builder;
import com.twilio.twiml.voice.Gather.Language;
import com.twilio.twiml.voice.Hangup;
import com.twilio.twiml.voice.Say;
import com.twilio.twiml.voice.Say.Voice;
import com.vonage.client.incoming.InputEvent;
import com.vonage.client.voice.TextToSpeechLanguage;
import com.vonage.client.voice.ncco.Ncco;
import com.vonage.client.voice.ncco.TalkAction;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class TelephonyService {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ServiceDetails serviceDetails;

  @Autowired
  private UserRequestService userRequestService;

  @Autowired
  private AnalyticsService analyticsService;

  @Autowired
  private FalconService falconService;

  @Autowired
  private TelephonyVonageRequestHandler telephonyVonageRequestHandler;

  @Autowired
  private TelephonyOzonetelRequestHandler telephonyOzonetelRequestHandler;

  @Autowired
  private RedisTemplate redisTemplate;

  @Autowired
  private SessionService sessionService;

  @Autowired
  private DEService deService;

  @Autowired
  private TelephonyVonageRenderer telephonyVonageRenderer;

  @Autowired
  private TelephonyOzonetelRenderer telephonyOzonetelRenderer;

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Autowired
  private RestUtils restUtils;

  @Value("${twilio.accountSid}")
  private String accountSid;

  @Value("${twilio.authToken}")
  private String authToken;


  private static final String SESSION_KEY_PREFIX = "evs:session::";
  private static final String TELEPHONY_URI_FORMAT = "%s/event/%s/voice/%s";
  private static final String TELEPHONY_CALL_STATUS = "CallStatus";
  private static final String TELEPHONY_CRASH_MESSAGE = "Something went wrong";
  private static final String TELEPHONY_RECORD_URI_FORMAT =
      "https://api.twilio.com/2010-04-01/Accounts/%s/Calls/%s/Recordings.json";

  public String processTwilioVoiceRequest(String applicationId, String language, String ntts,
      String speachTimeout, String ssmlBreak, Boolean bargeIn, Boolean record, String speechModel,
      MultiValueMap<String, String> params) {

    try {

      URI uri = getURIForTwilio(applicationId, language, ntts, speachTimeout, ssmlBreak, bargeIn,
          speechModel);
      Map<String, String> request = params.entrySet().stream()
          .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));
      request.put("language", language);
      request.put("ntts", ntts);
      request.put("uri", uri.toString());
      request.put("bargeIn", bargeIn.toString());
      request.put("speechModel", speechModel);

      if (Objects.equals(params.getFirst(TELEPHONY_CALL_STATUS), "completed")) {
        String fromNumber = request.get("From");
        String userId = "voice_".concat(fromNumber);
        DERequest deRequest = new DERequest();
        com.google.i18n.phonenumbers.Phonenumber.PhoneNumber phoneNumber =
            PhoneUtils.getPhoneNumber(fromNumber);
        // format the phone number
        deRequest.setPhoneNumberWithCountryCode(fromNumber);
        deRequest.setPhoneNumberCountryCode(String.valueOf(phoneNumber.getCountryCode()));
        deRequest.setPhoneNumber(String.valueOf(phoneNumber.getNationalNumber()));
        deRequest.setLogMe(true);
        Tenant tenant = falconService.getTenantByProjectId(applicationId);

        UserDetails userDetails = analyticsService.getUserDetails(applicationId, userId,
            Source.telephony.name(), tenant.getId(), deRequest);
        Session session = sessionService.getSession(applicationId,
            userDetails.getUserProfile().getUniversalUserId(), Channel.TELEPHONY);

        // publish to abandoned response to analytics during call completion
        analyticsStreamPublisher.publishAbandonedResponsesAnalytics(applicationId,
            Channel.TELEPHONY.getName(), userId, userDetails.getUserProfile().getUniversalUserId(),
            session.getSessionId(), session.isInsideForm(), session.isInsideQuiz());
        return null;
      }

      BotTelephonyTwilioResponse botTelephonyTwilioResponse =
          (BotTelephonyTwilioResponse) userRequestService.processUserRequest(applicationId, request,
              Channel.TELEPHONY, Vendor.TWILIO.name());

      if (Boolean.TRUE.equals(record)) {
        CompletableFuture.supplyAsync(() -> {
          recordCall(params);
          return 1;
        });
      }

      return botTelephonyTwilioResponse.getXmlResponse();
    } catch (Exception e) {
      log.error("Error occured while processing call request from twilio {}", e);
      analyticsStreamPublisher.publishErrorLogssAnalytics(getBotResponse(applicationId),
          e.getMessage());
    }
    com.twilio.twiml.voice.Say.Builder say = new Say.Builder("Sorry something went wrong");
    setSayLanguage(say, language);
    setVoice(say, ntts);
    return new VoiceResponse.Builder().say(say.build()).hangup(new Hangup.Builder().build()).build()
        .toXml();
  }

  public void recordCall(MultiValueMap<String, String> params) {
    try {
      StringBuilder uriBuf = new StringBuilder(
          String.format(TELEPHONY_RECORD_URI_FORMAT, accountSid, params.getFirst("CallSid")));

      Map<String, String> request = new HashMap<>();
      request.put("RecordingStatusCallbackEvent", "completed");

      Properties headerProperties = new Properties();
      headerProperties.put("Authorization",
          "Basic " + Base64.getEncoder().encodeToString((accountSid + ":" + authToken).getBytes()));

      restUtils.postRequest(uriBuf.toString(), objectMapper.writeValueAsString(request),
          MediaType.APPLICATION_FORM_URLENCODED, headerProperties, String.class);
    } catch (Exception e) {
      log.error(String.format("Unable to record the call %s", e));
    }
  }

  private URI getURIForTwilio(String applicationId, String language, String ntts,
      String speachTimeout, String ssmlBreak, Boolean bargeIn, String speechModel)
      throws URISyntaxException {

    UriComponentsBuilder requestUriBuilder =
        UriComponentsBuilder.fromHttpUrl(String.format(TELEPHONY_URI_FORMAT,
            serviceDetails.getEventservice().getUri(), "twilio", applicationId));

    if (StringUtils.hasText(language)) {
      requestUriBuilder.queryParam("language", language);
    }
    if (StringUtils.hasText(ntts)) {
      requestUriBuilder.queryParam("ntts", ntts);
    }
    if (StringUtils.hasText(speachTimeout)) {
      requestUriBuilder.queryParam("speachTimeout", speachTimeout);
    }
    if (StringUtils.hasText(ssmlBreak)) {
      requestUriBuilder.queryParam("ssmlBreak", ssmlBreak);
    }
    if (!ObjectUtils.isEmpty(bargeIn)) {
      requestUriBuilder.queryParam("bargeIn", bargeIn);
    }
    if (StringUtils.hasText(speechModel)) {
      requestUriBuilder.queryParam("speechModel", speechModel);
    }

    return new URI(requestUriBuilder.build().toUriString());
  }

  private void setGatherLanguage(Builder gather, String language) {
    if (StringUtils.hasText(language)) {
      gather.language(Constants.TelephonyConstants.gatherLanguage.get(language));
    } else {
      gather.language(Language.EN_US);
    }
  }


  public void setVoice(com.twilio.twiml.voice.Say.Builder say, String ntts) {

    say.voice(Constants.TelephonyConstants.voice.getOrDefault(ntts, Voice.POLLY_JOANNA_NEURAL));
  }

  public void setSayLanguage(com.twilio.twiml.voice.Say.Builder say, String language) {

    if (StringUtils.hasText(language)) {
      say.language(Constants.TelephonyConstants.sayLanguage.get(language));
    } else {
      say.language(com.twilio.twiml.voice.Say.Language.EN_US);
    }
  }

  public String processVonageIncomingCall(String applicationId, String language, Integer style,
      Integer maxDuration, Integer endOnSilence, InputEvent event) {

    try {
      StringBuilder uriBuf =
          getURIForVonage(applicationId, style, maxDuration, endOnSilence, language);

      DERequest deRequest = telephonyVonageRequestHandler.getDeRequest(applicationId, event);

      DEBotResponse deResponse = deService.postDeRequest(deRequest);
      log.debug("Received the botreply from DE");

      return telephonyVonageRenderer.render(deRequest, deResponse, language, style, maxDuration,
          endOnSilence, uriBuf);
    } catch (Exception e) {
      log.error("Error occurred while processing vonage request {}", e);
      analyticsStreamPublisher.publishErrorLogssAnalytics(getBotResponse(applicationId),
          e.getMessage());
    }
    TalkAction talkAction = new TalkAction.Builder(TELEPHONY_CRASH_MESSAGE)
        .language(getTextSpeachLanguage(language)).bargeIn(true).style(style).build();
    String response = new Ncco(talkAction).toJson();
    log.info("response result {}", response);
    return response;
  }

  private StringBuilder getURIForVonage(String applicationId, Integer style, Integer maxDuration,
      Integer endOnSilence, String language) {
    StringBuilder uriBuf = new StringBuilder(String.format(TELEPHONY_URI_FORMAT,
        serviceDetails.getEventservice().getUri(), "vonage", applicationId));

    if (ObjectUtils.isEmpty(style)) {
      style = 4;
    }
    if (ObjectUtils.isEmpty(maxDuration)) {
      maxDuration = 5;
    }
    if (ObjectUtils.isEmpty(endOnSilence)) {
      endOnSilence = 5;
    }
    if (!StringUtils.hasText(language)) {
      language = "en-US";
    }

    StringJoiner queryParamsJoiner = new StringJoiner("&");
    queryParamsJoiner.add("language=" + language);
    queryParamsJoiner.add("style=" + style);
    queryParamsJoiner.add("max_duration=" + maxDuration);
    queryParamsJoiner.add("end_on_silence=" + endOnSilence);

    if (queryParamsJoiner.length() > 0)
      uriBuf.append("?").append(queryParamsJoiner.toString());
    return uriBuf;
  }

  public TextToSpeechLanguage getTextSpeachLanguage(String language) {

    return Constants.TelephonyConstants.textSpeechLanguage.getOrDefault(language,
        TextToSpeechLanguage.AMERICAN_ENGLISH);
  }

  public com.vonage.client.voice.ncco.SpeechSettings.Language getSpeachLanguage(String language) {

    return Constants.TelephonyConstants.speechLanguage.getOrDefault(language,
        com.vonage.client.voice.ncco.SpeechSettings.Language.ENGLISH_UNITED_STATES);
  }

  public Object processOzonetelVoiceRequest(String applicationId, String event, String data,
      String error, String lang, String fromNumber, String speed, String type,
      String actionOnSilent, String silentCount, String silentGoodbyeMessage, String timeout,
      String speechCompleteTimeout, String speechIncompleteTimeout) throws Exception {


    if (!StringUtils.hasText(silentCount)) {
      silentCount = "3";
    }
    if (!StringUtils.hasText(lang)) {
      lang = "en-IN";
    }

    try {
      DERequest deRequest = telephonyOzonetelRequestHandler.getDeRequest(applicationId, event,
          fromNumber, data, silentCount);

      log.debug("Received {} request from ozonetel for user {} ", event, data);
      if (event.equals("Recognize") && "noinput".equals(error)) {
        // Handling silence
        Session session = null;
        String sessionKey = SESSION_KEY_PREFIX
            .concat(Arrays.asList(Channel.TELEPHONY.name(), deRequest.getUniversalUserId(),
                deRequest.getProjectId()).stream().collect(Collectors.joining(",")));
        Object sessionStr = redisTemplate.opsForValue().get(sessionKey);

        if (!ObjectUtils.isEmpty(sessionStr)) {
          session = objectMapper.readValue(sessionStr.toString(), Session.class);
          session.setSilentCount(session.getSilentCount() - 1);
          sessionService.updateSession(deRequest, Channel.TELEPHONY, session);

          if (session.getSilentCount() < 0) {
            // if silence count < 0 hang up the call
            return telephonyOzonetelRenderer.render(silentGoodbyeMessage, lang, speed, type,
                timeout, speechCompleteTimeout, speechIncompleteTimeout, true);
          }
        }

        return telephonyOzonetelRenderer.render(actionOnSilent, lang, speed, type, timeout,
            speechCompleteTimeout, speechIncompleteTimeout, false);
      } else if (event.equals("Disconnect") || event.equals("Hangup")) {
        // publish to abandoned response to analytics during hangup
        Session session = sessionService.getSession(deRequest.getProjectId(),
            deRequest.getUniversalUserId(), Channel.TELEPHONY);
        analyticsStreamPublisher.publishAbandonedResponsesAnalytics(deRequest.getProjectId(),
            Channel.TELEPHONY.getName(), deRequest.getUserId(), deRequest.getUniversalUserId(),
            session.getSessionId(), session.isInsideForm(), session.isInsideQuiz());
        return null;
      }

      DEBotResponse deResponse = deService.postDeRequest(deRequest);

      boolean isDialogExit = false;
      if (!ObjectUtils.isEmpty(deResponse.getIsDialogExit())
          && Boolean.TRUE.equals(deResponse.getIsDialogExit())) {

        isDialogExit = true;
      }

      StringBuilder textBuilder = new StringBuilder();
      deResponse.getBotReply().getText().stream()
          .forEach(text -> textBuilder.append(text.trim()).append(System.lineSeparator()));
      return telephonyOzonetelRenderer.render(textBuilder.toString(), lang, speed, type, timeout,
          speechCompleteTimeout, speechIncompleteTimeout, isDialogExit);
    } catch (Exception e) {
      log.error("Error occured while processing call request from ozonetel {}", e);
      analyticsStreamPublisher.publishErrorLogssAnalytics(getBotResponse(applicationId),
          e.getMessage());
    }

    return telephonyOzonetelRenderer.render(TELEPHONY_CRASH_MESSAGE, lang, speed, type, timeout,
        speechCompleteTimeout, speechIncompleteTimeout, true);

  }

  private BotResponse getBotResponse(String applicationId) {
    BotResponse botResponse = new BotResponse();
    botResponse.setProjectId(applicationId);
    botResponse.setSource(Source.telephony.name());
    return botResponse;
  }

}
