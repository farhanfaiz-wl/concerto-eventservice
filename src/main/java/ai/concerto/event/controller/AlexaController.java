package ai.concerto.event.controller;


import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ai.concerto.event.service.AlexaService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(path = "/event/alexa")
public class AlexaController {

  @Autowired
  private AlexaService alexaService;

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> handleAlexaRequest(
      @RequestHeader(value = "SignatureCertChainUrl",
          required = false) String signatureCertChainUrl,
      @RequestHeader(value = "Signature", required = false) String signature,
      @RequestHeader(value = "X-APP-NAME", required = false) String appName,
      @RequestBody String event) {

    log.debug("Rest API /event/alexa triggered.");
    try {
      String response =
          alexaService.processAlexaRequest(event, appName, signatureCertChainUrl, signature);
      MDC.put("response_time", String.valueOf(
          System.currentTimeMillis() - Long.valueOf((String) MDC.get("request_timestamp_ms"))));
      log.info("Rest API /event/alexa processed.");
      return ResponseEntity.ok(response);
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
  }
}
