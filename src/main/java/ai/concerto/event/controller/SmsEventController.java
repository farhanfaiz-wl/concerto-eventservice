package ai.concerto.event.controller;

import java.util.Map;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Message;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.exchange.BotSmsResponse;
import ai.concerto.event.exchange.SmsUserRequest;
import ai.concerto.event.handler.response.SmsTwilioResponseHandler;
import ai.concerto.event.publisher.UserRequestStreamPublisher;
import ai.concerto.event.service.LobbyService;
import ai.concerto.event.service.SmsService;
import ai.concerto.event.service.UserRequestService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(path = "/event")
@Slf4j
public class SmsEventController {

  private static final String PROJECT_ID = "project_id";

  @Autowired
  private UserRequestStreamPublisher userRequestStreamPublisher;

  @Autowired
  private SmsTwilioResponseHandler smsTwilioResponseHandler;

  @Autowired
  private SmsService smsService;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRequestService userRequestService;

  @Autowired
  private LobbyService lobbyService;

  @Value("${twilio.accountSid}")
  private String accountSid;

  @Value("${twilio.authToken}")
  private String authToken;

  @SneakyThrows
  @PostMapping(value = {"/sms/twilio/{applicationId}", "/{applicationId}/twilio/sms"},
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<Void> processSmsTwilioRequest(@PathVariable String applicationId,
      @RequestBody MultiValueMap<String, String> params) {
    MDC.put(PROJECT_ID, applicationId);
    SmsUserRequest smsUserRequest = new SmsUserRequest(params.getFirst("From"),
        params.getFirst("To"), params.getFirst("ProfileName"), params.getFirst("Body"));
    userRequestStreamPublisher.publishUserRequest(applicationId,
        objectMapper.writeValueAsString(smsUserRequest), Channel.SMS, Vendor.TWILIO.name());
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/sendMessage", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> sendMessageViaTwilio(@RequestBody Map<String, String> request) {

    BotSmsResponse botSmsResponse = new BotSmsResponse();
    botSmsResponse.setAuthId(accountSid);
    botSmsResponse.setAuthToken(authToken);
    botSmsResponse.setFrom(request.get("from_phone_number"));
    botSmsResponse.setTo(request.get("to_phone_number"));
    botSmsResponse.setMessage(request.get("message"));
    smsTwilioResponseHandler.postBotResponse(botSmsResponse);
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/{applicationId}/twilio/smsbot",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      produces = "application/xml;charset=utf-8")
  public ResponseEntity<String> processSmsTwilioSmsBot(@PathVariable String applicationId,
      @RequestBody MultiValueMap<String, String> params) throws Exception {
    MDC.put(PROJECT_ID, applicationId);
    SmsUserRequest smsUserRequest = new SmsUserRequest(params.getFirst("From"),
        params.getFirst("To"), params.getFirst("ProfileName"), params.getFirst("Body"));
    String response = smsService.fetchBotResponseFromRipples(applicationId, smsUserRequest);
    if (StringUtils.hasText(response)) {
      return ResponseEntity.ok(new MessagingResponse.Builder()
          .message(new Message.Builder().body(new Body.Builder(response).build()).build()).build()
          .toXml());
    } else {
      return ResponseEntity.ok().build();
    }
  }

  @PostMapping(path = "/sms/twilio/status/{applicationId}",
      produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<Void> processTwilioMessageStatus(@PathVariable String applicationId,
      @RequestBody MultiValueMap<String, String> params) {
    smsService.handleAgentSmsFailure(applicationId, params);
    lobbyService.updateTwilioMessageStatus(applicationId, params);
    return ResponseEntity.ok().build();
  }
}
