package ai.concerto.event.controller;

import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ai.concerto.event.exchange.OgTagsResponse;
import ai.concerto.event.exchange.TextToSpeechRequest;
import ai.concerto.event.service.AllegroUiApiService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/event")
public class AllegroUiApiController {

  private static final String RESPONSE_TIME = "response_time";
  private static final String REQUEST_TIMESTAMP = "request_timestamp_ms";

  @Autowired
  private AllegroUiApiService allegroUiApiService;

  @PostMapping(path = "/tts", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> fetchTts(@RequestBody TextToSpeechRequest request) {
    MDC.put(RESPONSE_TIME, String
        .valueOf(System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP))));
    Object response = allegroUiApiService.convertTextToSpeech(request);
    log.info("Rest POST API /event/tts processed.");
    return ResponseEntity.ok(response);
  }

  @GetMapping(path = "/ogtags", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<OgTagsResponse> fetchOgTags(
      @RequestParam(value = "url", required = true) String url) {
    MDC.put(RESPONSE_TIME, String
        .valueOf(System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP))));
    OgTagsResponse response = allegroUiApiService.getOgTags(url);
    log.info("Rest POST API /event/ogtags processed.");
    return ResponseEntity.ok(response);
  }
}
