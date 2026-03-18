package ai.concerto.event.controller;

import ai.concerto.event.exchange.BotConversationEndResponse;
import ai.concerto.event.exchange.BotConversationRequest;
import ai.concerto.event.exchange.BotConversationResponse;
import ai.concerto.event.exchange.BotConversationStartRequest;
import ai.concerto.event.exchange.BotConversationStartResponse;
import ai.concerto.event.service.BotConversationService;
import ai.concerto.event.service.FalconService;
import ai.concerto.event.service.SereneService;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v0/bot")
public class BotConversationController {

  private static final String PROJECT_ID = "project_id";
  private static final String RESPONSE_TIME = "response_time";
  private static final String REQUEST_TIMESTAMP = "request_timestamp_ms";

  @Autowired
  private BotConversationService botConversationService;

  @Autowired
  private SereneService sereneService;

  @Autowired
  private FalconService falconService;

  @PostMapping(path = "/conversations", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BotConversationStartResponse> newConversation(
      @RequestBody BotConversationStartRequest request) {
    BotConversationStartResponse response = botConversationService.newConversation(request);
    MDC.put(RESPONSE_TIME, String
        .valueOf(System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP))));
    log.info("Rest POST API /v0/bot/conversations processed.");
    return ResponseEntity.ok(response);
  }

  @PostMapping(path = "/conversations/{conversationId}/messages",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BotConversationResponse> generateBotResponse(
      @PathVariable("conversationId") String conversationId,
      @RequestBody BotConversationRequest request) throws Exception {
    BotConversationResponse resposne =
        botConversationService.generateBotResponse(conversationId, request);
    MDC.put(RESPONSE_TIME, String
        .valueOf(System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP))));
    log.info("Rest POST API /v0/bot/conversations/{}/messages processed.", conversationId);
    return ResponseEntity.ok(resposne);
  }

  @DeleteMapping(path = "/conversations/{conversationId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> deleteConversation(
      @PathVariable("conversationId") String conversationId) {
    BotConversationEndResponse response = botConversationService.deleteConversation(conversationId);
    MDC.put(RESPONSE_TIME, String
        .valueOf(System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP))));
    log.info("Rest DELETE API /v0/bot/conversations/{} processed.", conversationId);
    return ResponseEntity.ok(response);
  }

  @PostMapping(path = "/projects/{project_id}/schemas/{schema_id}/search",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> fetchSchemaSearch(@RequestHeader("X-API-TOKEN") String apiToken,
      @PathVariable("project_id") String projectId, @PathVariable("schema_id") String schemaId,
      @RequestParam(value = "q", required = false) String query,
      @RequestParam(value = "page_no", required = false) Integer pageNo,
      @RequestParam(value = "page_size", required = false) Integer pageSize,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "include_qa_matches", required = false) Boolean includeQaMatches,
      @RequestParam(value = "user_id", required = false) String userId,
      @RequestBody(required = false) String request) {
    MDC.put(PROJECT_ID, projectId);
    if (!StringUtils.hasText(userId))
      userId = apiToken;
    String response = sereneService.fetchSchemaSearch(projectId, userId, query, schemaId, request,
        sort, includeQaMatches, pageNo, pageSize);
    MDC.put(RESPONSE_TIME, String
        .valueOf(System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP))));
    log.info("Rest POST API /projects/{}/schemas/{}/search processed.", projectId, schemaId);
    return ResponseEntity.ok(response);
  }

  @PostMapping(path = "/projects/{project_id}/search", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> fetchUniversalSearch(@RequestHeader("X-API-TOKEN") String apiToken,
      @PathVariable(value = "project_id") String projectId,
      @RequestParam(value = "q", required = false) String query,
      @RequestParam(value = "schemas", required = false) String schemas,
      @RequestParam(value = "page_no", required = false) Integer pageNo,
      @RequestParam(value = "page_size", required = false) Integer pageSize,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "include_qa_matches", required = false) Boolean includeQaMatches,
      @RequestParam(value = "user_id", required = false) String userId) {
    MDC.put(PROJECT_ID, projectId);
    if (!StringUtils.hasText(userId))
      userId = apiToken;

    String response = sereneService.fetchUniversalSearchResult(projectId, userId, query, pageNo,
        pageSize, sort, includeQaMatches, schemas);
    MDC.put(RESPONSE_TIME, String
        .valueOf(System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP))));
    log.info("Rest POST API projects/{}/search processed.", projectId);
    return ResponseEntity.ok(response);
  }

  @PostMapping(path = "/projects/{project_id}/autocomplete",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> fetchUniversalAutoComplete(
      @PathVariable("project_id") String projectId,
      @RequestParam(value = "q", required = false) String query,
      @RequestParam(value = "limit", required = false) Integer limit) {
    MDC.put(PROJECT_ID, projectId);
    String response = sereneService.fetchUniversalAutoComplete(projectId, query, limit);
    MDC.put(RESPONSE_TIME, String
        .valueOf(System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP))));
    log.info("Rest POST API projects/{}/autocomplete processed.", projectId);
    return ResponseEntity.ok(response);
  }

  @PostMapping(path = "/projects/{project_id}/schemas/{schema_id}/autocomplete",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> fetchSchemaAutoComplete(
      @PathVariable("project_id") String projectId, @PathVariable("schema_id") String schemaId,
      @RequestParam(value = "q", required = false) String query,
      @RequestParam(value = "limit", required = false) Integer limit) {
    MDC.put(PROJECT_ID, projectId);
    String response = sereneService.fetchSchemaAutoComplete(projectId, schemaId, query, limit);
    MDC.put(RESPONSE_TIME, String
        .valueOf(System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP))));
    log.info("Rest POST API /projects/{}/schemas/{}/autocomplete processed.", projectId, schemaId);
    return ResponseEntity.ok(response);
  }

}
