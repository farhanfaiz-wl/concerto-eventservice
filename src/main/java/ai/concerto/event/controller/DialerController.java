package ai.concerto.event.controller;

import ai.concerto.event.dto.DialerToken;
import ai.concerto.event.service.DialerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/event/dialer")
public class DialerController {

  @Autowired
  private DialerService dialerService;

  @GetMapping("/token")
  public ResponseEntity<DialerToken> refreshToken() {
    return ResponseEntity.ok(dialerService.getToken());
  }

  @PostMapping(path = "/dial", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> handleCall(@RequestBody MultiValueMap<String, String> params) {
    return ResponseEntity.ok(dialerService.handleDial(params));
  }

  @PostMapping(path = "/record", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<Void> handleRecord(@RequestParam("project_id") String projectId,
      @RequestParam("ticket_id") String ticketId,
      @RequestParam("ticket_number") String ticketNumber,
      @RequestParam("universal_user_id") String universalUserId,
      @RequestBody MultiValueMap<String, String> params) {
    dialerService.handleRecord(projectId, ticketId, ticketNumber, universalUserId, params);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PostMapping(path = "/transcribe", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<Void> handleTranscription(@RequestParam("AddOns") String request) {
    dialerService.handleTranscription(request);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

}
