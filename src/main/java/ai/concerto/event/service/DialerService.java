package ai.concerto.event.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VoiceGrant;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Dial;
import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.dto.DialerToken;
import ai.concerto.event.exchange.DialerTranscriptionDetails;
import ai.concerto.event.exchange.TwilioTranscriptionRequest;
import ai.concerto.event.exchange.TwilioTranscriptionResponse;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DialerService {

  private static final String RECORDING_STATUS_CALLBACK_URI =
      "%s/event/dialer/record?project_id=%s&ticket_id=%s&ticket_number=%s&universal_user_id=%s";
  private static final String DIALER_KEY_FORMATTER = "DIALER_%s";
  private static final String PAYLOAD = "payload";
  private static final String CALL_TRANSCRIPTION = "CALL_TRANSCRIPTION";
  private static final String CALL_AUDIO = "CALL_AUDIO";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private RestUtils restUtils;

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Value("${twilio.appSid}")
  private String twimlAppSid;

  @Value("${twilio.apiKeySid}")
  private String apiKeySid;

  @Value("${twilio.apiKeySecret}")
  private String apiKeySecret;

  @Value("${twilio.accountSid}")
  private String accountSid;

  @Value("${twilio.authToken}")
  private String authToken;

  @Value("${twilio.number}")
  private String dialerNumber;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Autowired
  private ServiceDetails serviceDetails;



  public DialerToken getToken() {
    VoiceGrant voiceGrant =
        new VoiceGrant().setOutgoingApplicationSid(twimlAppSid).setIncomingAllow(true);

    AccessToken accessToken = new AccessToken.Builder(accountSid, apiKeySid, apiKeySecret)
        .identity(dialerNumber).grant(voiceGrant).build();

    DialerToken dialerToken = new DialerToken();
    dialerToken.setToken(accessToken.toJwt());
    dialerToken.setIdentity(dialerNumber);

    return dialerToken;
  }

  public String handleDial(MultiValueMap<String, String> params) {

    try {
      log.info("Dial params {}", objectMapper.writeValueAsString(params));
      Dial dial =
          new Dial.Builder(params.getFirst("To")).callerId(dialerNumber)
              .record(Dial.Record.RECORD_FROM_RINGING_DUAL).trim(Dial.Trim.TRIM_SILENCE)
              .recordingStatusCallback(String.format(RECORDING_STATUS_CALLBACK_URI,
                  serviceDetails.getTelephony().getUri(), params.getFirst("project_id"),
                  params.getFirst("ticket_id"), params.getFirst("ticket_number"),
                  params.getFirst("universal_user_id")))
              .build();
      VoiceResponse response = new VoiceResponse.Builder().dial(dial).build();
      return response.toXml();
    } catch (Exception e) {
      log.error("Unable to dial {} due to {}", params.getFirst("To"), e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Unable to dial number using dialer", e);
    }
  }

  public void handleRecord(String projectId, String ticketId, String ticketNumber,
      String universalUserId, MultiValueMap<String, String> params) {

    try {

      DialerTranscriptionDetails transcriptionDetails =
          new DialerTranscriptionDetails(projectId, ticketId, ticketNumber, universalUserId,
              params.getFirst("CallSid"), params.getFirst("RecordingUrl"));
      redisTemplate.opsForValue().set(
          String.format(DIALER_KEY_FORMATTER, params.getFirst("RecordingSid")),
          objectMapper.writeValueAsString(transcriptionDetails), 300, TimeUnit.SECONDS);
      Map<String, Object> agentPayload = new HashMap<>();
      agentPayload.put(PAYLOAD,
          getuserMessagePayload(transcriptionDetails, null, "USER", CALL_AUDIO));
      analyticsStreamPublisher.publishAgentMessage(objectMapper.writeValueAsString(agentPayload));

    } catch (Exception e) {
      log.error("Unable to cache dialer call due to {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Unable to cache dialer call", e);
    }
  }

  public void handleTranscription(String request) {

    try {
      TwilioTranscriptionRequest transcriptionRequest =
          objectMapper.readValue(request, TwilioTranscriptionRequest.class);
      TwilioTranscriptionResponse transcriptionResponse =
          getVoicebaseTranscription(transcriptionRequest.getResults().getVoicebaseTranscription()
              .getPayload().get(0).get("url"));

      String transciption = transcriptionResponse.getMedia().getTranscripts().getText();
      Boolean isUserFirst = transciption.startsWith("Speaker 2");
      List<String> utterances = Arrays.asList(Arrays.stream(transciption.split("Speaker [1-2]: "))
          .filter(s -> !s.isEmpty()).toArray(String[]::new));

      String recordingSid = transcriptionRequest.getResults().getVoicebaseTranscription().getLinks()
          .get("recording").split("/Recordings/")[1];
      String dialerStr = objectMapper.convertValue(
          redisTemplate.opsForValue().get(String.format(DIALER_KEY_FORMATTER, recordingSid)),
          new TypeReference<String>() {});

      if (ObjectUtils.isEmpty(dialerStr)) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
      }

      DialerTranscriptionDetails transcriptionDetails =
          objectMapper.readValue(dialerStr, objectMapper.getTypeFactory()
              .constructType(new TypeReference<DialerTranscriptionDetails>() {}));

      Map<String, Object> agentReply = new HashMap<>();
      for (int i = 0; i < utterances.size(); i++) {
        if (Boolean.TRUE.equals(isUserFirst)) {
          if (i % 2 == 0) {
            agentReply.put(PAYLOAD, getuserMessagePayload(transcriptionDetails, utterances.get(i),
                "USER", CALL_TRANSCRIPTION));
          } else {
            agentReply.put(PAYLOAD, getuserMessagePayload(transcriptionDetails, utterances.get(i),
                "AGENT", CALL_TRANSCRIPTION));
          }
        } else {
          if (i % 2 == 0) {
            agentReply.put(PAYLOAD, getuserMessagePayload(transcriptionDetails, utterances.get(i),
                "AGENT", CALL_TRANSCRIPTION));
          } else {
            agentReply.put(PAYLOAD, getuserMessagePayload(transcriptionDetails, utterances.get(i),
                "USER", CALL_TRANSCRIPTION));
          }
        }

        analyticsStreamPublisher.publishAgentMessage(objectMapper.writeValueAsString(agentReply));
      }
    } catch (Exception e) {
      log.error("Unable to transcribe call due to {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Unable to transcribe call", e);
    }
  }


  public TwilioTranscriptionResponse getVoicebaseTranscription(String requestUrl) {

    Properties headerProperties = new Properties();
    headerProperties.put("Authorization",
        "Basic " + Base64.getEncoder().encodeToString((accountSid + ":" + authToken).getBytes()));

    try {
      return restUtils.getRequest(requestUrl, headerProperties, TwilioTranscriptionResponse.class);
    } catch (Exception e) {
      log.error("Unable to get transcription details {}", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Unable to get transcription details", e);
    }
  }

  private Map<String, Object> getuserMessagePayload(DialerTranscriptionDetails transcriptionDetails,
      String utterance, String actor, String messageType) {
    Map<String, Object> payLoad = new HashMap<>();
    Map<String, Object> message = new HashMap<>();
    Map<String, String> ticket = new HashMap<>();

    DateTimeFormatter sourceFormat =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'");
    DateTimeFormatter targetFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    LocalDateTime dateTime =
        LocalDateTime.parse(OffsetDateTime.now(ZoneOffset.UTC).toString(), sourceFormat);
    String formattedDateTime = dateTime.atZone(ZoneId.of("UTC")).format(targetFormat);

    message.put("text", utterance);
    message.put("actor", actor);
    message.put("type", messageType);
    if (CALL_AUDIO.equals(messageType)) {
      Optional.ofNullable(transcriptionDetails.getLink())
          .ifPresent(lnk -> message.put("link", lnk));
    }
    Optional.ofNullable(transcriptionDetails.getCallSid())
        .ifPresent(sid -> message.put("call_sid", sid));
    message.put("time_stamp", formattedDateTime);
    ticket.put("id", transcriptionDetails.getTicketId());
    ticket.put("number", transcriptionDetails.getTicketNumber());

    payLoad.put("project_id", transcriptionDetails.getProjectId());
    payLoad.put("channel", "telephony");
    payLoad.put("universal_user_id", transcriptionDetails.getUniversalUserId());
    payLoad.put("message", message);
    payLoad.put("ticket", ticket);

    return payLoad;

  }
}
