package ai.concerto.event.controller;

import ai.concerto.event.dto.StructuredSearchRequest;
import ai.concerto.event.service.StructuredSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v0/bot/search")
public class StructuredSearchController {

  @Autowired private StructuredSearchService structuredSearchService;

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> fetchStructuredSearch(
      @RequestParam(name = "text", required = false) String text,
      @RequestParam(name = "size", required = false) Integer size,
      @RequestParam(name = "scroll_ttl", required = false) String scrollTTL,
      @RequestBody StructuredSearchRequest request) {

    return ResponseEntity.ok(
        structuredSearchService.fetchStructuredSearchFromSeren(text, size, scrollTTL, request));
  }
}
