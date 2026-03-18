package ai.concerto.event.controller;

import ai.concerto.event.service.TelephonyService;
import com.vonage.client.incoming.InputEvent;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/event")
public class TelephonyController {

  public static final String PROJECT_ID = "project_id";

  @Autowired
  private TelephonyService telephonyService;

  @PostMapping(path = {"/twilio/voice/{applicationId}", "/twilio/{applicationId}/voice"},
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> handleCallResponse(
      @PathVariable("applicationId") String applicationId,
      @RequestParam(value = "language", required = false) String language,
      @RequestParam(value = "ntts", required = false) String ntts,
      @RequestParam(value = "speachTimeout", required = false) String speachTimeout,
      @RequestParam(value = "ssmlBreak", required = false) String ssmlBreak,
      @RequestParam(value = "bargeIn", required = false, defaultValue = "true") Boolean bargeIn,
      @RequestParam(value = "record", required = false, defaultValue = "false") Boolean record,
      @RequestParam(value = "speechModel", required = false,
          defaultValue = "phone_call") String speechModel,
      @RequestBody MultiValueMap<String, String> params) {
    MDC.put(PROJECT_ID, applicationId);
    return ResponseEntity.ok(telephonyService.processTwilioVoiceRequest(applicationId, language,
        ntts, speachTimeout, ssmlBreak, bargeIn, record, speechModel, params));
  }

  @PostMapping(path = {"/vonage/voice,{applicationId}", "/vonage/{applicationId}/voice"},
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> handleVonageRequest(
      @PathVariable("applicationId") String applicationId,
      @RequestParam(value = "language", required = false) String language,
      @RequestParam(value = "style", required = false) Integer style,
      @RequestParam(value = "max_duration", required = false) Integer maxDuration,
      @RequestParam(value = "end_on_silence", required = false) Integer endOnSilence,
      @RequestBody InputEvent event) {
    MDC.put(PROJECT_ID, applicationId);
    return ResponseEntity.ok(telephonyService.processVonageIncomingCall(applicationId, language,
        style, maxDuration, endOnSilence, event));
  }

  @GetMapping(path = {"/ozonetel/voice/{applicationId}", "/ozonetel/{applicationId}/voice"},
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<Object> handleOzonetelResponse(
      @PathVariable("applicationId") String applicationId, @RequestParam("event") String event,
      @RequestParam(value = "data", required = false) String data,
      @RequestParam(value = "error", required = false) String error,
      @RequestParam(value = "lang", required = false) String lang,
      @RequestParam(value = "cid", required = false) String fromNumber,
      @RequestParam(value = "speed", required = false) String speed,
      @RequestParam(value = "type", required = false) String type,
      @RequestParam(value = "welcomePrompt", required = false) String welcomePrompt,
      @RequestParam(value = "actionOnSilent", required = false) String actionOnSilent,
      @RequestParam(value = "silentCount", required = false) String silentCount,
      @RequestParam(value = "silentGoodbyeMessage", required = false) String silentGoodbyeMessage,
      @RequestParam(value = "timeout", required = false) String timeout,
      @RequestParam(value = "speechCompleteTimeout", required = false) String speechCompleteTimeout,
      @RequestParam(value = "speechIncompleteTimeout",
          required = false) String speechIncompleteTimeout)
      throws Exception {
    MDC.put(PROJECT_ID, applicationId);
    return ResponseEntity.ok(telephonyService.processOzonetelVoiceRequest(applicationId, event,
        data, error, lang, fromNumber, speed, type, actionOnSilent, silentCount,
        silentGoodbyeMessage, timeout, speechCompleteTimeout, speechIncompleteTimeout));
  }

}
