package ai.concerto.event.controller;

import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.exchange.WhatsappCloudApiRequest;
import ai.concerto.event.exchange.WhatsappUserRequest;
import ai.concerto.event.publisher.UserRequestStreamPublisher;
import ai.concerto.event.service.LobbyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/event")
public class WhatsappEventController {


  public static final String RESPONSE_MESSAGE = "message-published";
  public static final String PROJECT_ID = "project_id";

  @Autowired
  private UserRequestStreamPublisher userRequestStreamPublisher;

  @Autowired
  private LobbyService lobbyService;

  @Autowired
  private ObjectMapper objectMapper;

  @GetMapping("/whatsapp/kaleyra/{applicationId}")
  public String processWhatsappKaleyraRequest(@PathVariable String applicationId,
      @RequestParam String from, @RequestParam String name, @RequestParam String type,
      @RequestParam String body, @RequestParam(value = "created_at") String createdAt,
      @RequestParam(value = "reply_to") String replyTo) throws Exception {
    MDC.put(PROJECT_ID, applicationId);
    WhatsappUserRequest request =
        new WhatsappUserRequest(from, name, type, body, createdAt, replyTo);
    userRequestStreamPublisher.publishUserRequest(applicationId,
        objectMapper.writeValueAsString(request), Channel.WHATSAPP, Vendor.KALEYRA.name());
    return RESPONSE_MESSAGE;
  }

  @PostMapping({"/whatsapp/route/{applicationId}", "/{applicationId}/whatsapp/route"})
  public String processWhatsappRouteRequest(@PathVariable String applicationId,
      @RequestBody String request) {
    MDC.put(PROJECT_ID, applicationId);
    userRequestStreamPublisher.publishUserRequest(applicationId, request, Channel.WHATSAPP,
        Vendor.ROUTE.name());
    return RESPONSE_MESSAGE;
  }


  @SneakyThrows
  @GetMapping({"/whatsapp/vf/{applicationId}", "/{applicationId}/vf/whatsapp"})
  public ResponseEntity<Void> processWhatsappVFRequest(
      @PathVariable("applicationId") String applicationId, @RequestParam String to,
      @RequestParam String from, @RequestParam(value = "Text") String text,
      @RequestParam(value = "content_type") String contentType,
      @RequestParam(value = "media_type") String mediaType,
      @RequestParam(value = "media_data") String mediaData, @RequestParam String latitude,
      @RequestParam String longitude) {
    MDC.put(PROJECT_ID, applicationId);
    Map<String, String> eventMap = new HashMap<>();
    eventMap.put("to", to);
    eventMap.put("from", from);
    eventMap.put("text", text);

    log.info("Received whatsapp vf request: {}", objectMapper.writeValueAsString(eventMap));
    userRequestStreamPublisher.publishUserRequest(applicationId,
        objectMapper.writeValueAsString(eventMap), Channel.WHATSAPP, Vendor.VF.name());
    return ResponseEntity.ok().build();

  }


  @PostMapping(value = {"/whatsapp/twilio/{applicationId}", "/{applicationId}/twilio/whatsapp"},
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<Void> processWhatsappTwilioRequest(@PathVariable String applicationId,
      @RequestBody MultiValueMap<String, String> params) throws JsonProcessingException {
    MDC.put(PROJECT_ID, applicationId);
    WhatsappUserRequest whatsappUserRequest = new WhatsappUserRequest(params.getFirst("From"),
        params.getFirst("ProfileName"), null, params.getFirst("Body"), null, params.getFirst("To"));
    userRequestStreamPublisher.publishUserRequest(applicationId,
        objectMapper.writeValueAsString(whatsappUserRequest), Channel.WHATSAPP,
        Vendor.TWILIO.name());
    return ResponseEntity.ok().build();

  }

  @GetMapping("/whatsapp/cloud_api/{applicationId}")
  public ResponseEntity<String> processCloudApiWebhookVerification(
      @RequestParam("hub.mode") String mode, @RequestParam("hub.challenge") String challenge,
      @RequestParam("hub.verify_token") String verifyToken) {

    if (mode.equalsIgnoreCase("subscribe") && verifyToken.equals("C52ZgR7VaQAS")) {

      return (ResponseEntity.ok(challenge));
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

  }

  @PostMapping("/whatsapp/cloud_api/{applicationId}")
  public ResponseEntity<String> processCloudApiRequest(@PathVariable String applicationId,
      @RequestBody String request) throws Exception {
    log.info("Received whatsapp cloud api request: {}", request);
    MDC.put(PROJECT_ID, applicationId);
    WhatsappCloudApiRequest whatsappCloudApiRequest =
        objectMapper.readValue(objectMapper.readTree(request).get("entry").get(0).get("changes")
            .get(0).get("value").toString(), new TypeReference<WhatsappCloudApiRequest>() {});
    if (ObjectUtils.isEmpty(whatsappCloudApiRequest.getMessages())) {
      return ResponseEntity.ok().build();
    }
    userRequestStreamPublisher.publishUserRequest(applicationId, objectMapper.readTree(request)
        .get("entry").get(0).get("changes").get(0).get("value").toString(), Channel.WHATSAPP,
        Vendor.CLOUD_API.name());
    return ResponseEntity.ok().build();
  }

  @PostMapping(path = "/whatsapp/twilio/status/{applicationId}",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<Void> updateTwilioMessageStatus(@PathVariable String applicationId,
      @RequestBody MultiValueMap<String, String> params) {
    log.info("Received twilio message status for app id: {}", applicationId);
    lobbyService.updateTwilioMessageStatus(applicationId, params);
    return ResponseEntity.ok().build();
  }
}
