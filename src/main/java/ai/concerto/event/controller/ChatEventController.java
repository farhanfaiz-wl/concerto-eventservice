package ai.concerto.event.controller;

import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.ChatPreIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.ChatbotIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.ChatbotIntegrationForUI;
import ai.concerto.event.exchange.BotChatBotResponse;
import ai.concerto.event.exchange.ChatClientEventRequest;
import ai.concerto.event.exchange.DataSet;
import ai.concerto.event.handler.response.ChatClientEventResponse;
import ai.concerto.event.publisher.UserRequestStreamPublisher;
import ai.concerto.event.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.util.BadRequestException;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/event/chatbot")
public class ChatEventController {


  private static final String PROJECT_ID = "project_id";
  private static final String RESPONSE_TIME = "response_time";
  private static final String REQUEST_TIMESTAMP_MS = "request_timestamp_ms";

  @Autowired
  private AnalyticsService analyticsService;

  @Autowired
  private UserRequestStreamPublisher userRequestStreamPublisher;

  @Autowired
  private UserRequestService userRequestService;

  @Autowired
  private KrishnaService krishnaService;

  @Autowired
  private IntegrationService integrationService;

  @Autowired
  private ChatBotService chatBotService;

  @Autowired
  @Qualifier("objectMapper")
  private ObjectMapper objectMapper;

  @PostMapping(path = "/{applicationId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public BotChatBotResponse processChatEvent(@PathVariable String applicationId,
      @RequestBody DERequest request) throws Exception {
    MDC.put(PROJECT_ID, applicationId);
    log.debug("Rest POST API /event/chatbot/{} triggered", applicationId);
    BotChatBotResponse response = (BotChatBotResponse) userRequestService
        .processUserRequest(applicationId, request, Channel.CHATBOT, null);
    MDC.put(RESPONSE_TIME, String.valueOf(
        System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP_MS))));
    log.info("Rest POST API /event/chatbot/{} processed", applicationId);
    return response;
  }


  @SneakyThrows
  @PostMapping("/publish")
  public String publishChatEvent(@RequestBody DERequest request) {
    MDC.put(PROJECT_ID, request.getProjectId());
    userRequestStreamPublisher.publishUserRequest(request.getProjectId(),
        objectMapper.writeValueAsString(request), Channel.CHATBOT, null);
    return "message-published";
  }

  @SneakyThrows
  @PostMapping(path = "/client", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ChatClientEventResponse> publishChatClientEvent(
      @RequestParam(value = "log_me", required = false) Boolean logMe,
      @RequestBody ChatClientEventRequest request) {
    log.debug("Rest POST API /event/chatbot/client triggered");
    if (ObjectUtils.isEmpty(logMe)) {
      logMe = false;
    }
    ChatClientEventResponse response = analyticsService.postChatClientEvent(request, logMe);
    MDC.put(RESPONSE_TIME, String.valueOf(
        System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP_MS))));
    log.info("Rest POST API /event/chatbot/client processed");
    return ResponseEntity.ok(response);
  }

  @GetMapping(path = "/{applicationId}/krishna", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getRecommendationsFromKrishna(
      @PathVariable("applicationId") String applicationId,
      @RequestParam(value = "userId", required = false) String userId) {
    MDC.put(PROJECT_ID, applicationId);
    log.debug("Rest GET API /event/chatbot/{}/krishna triggered", applicationId);
    if (StringUtils.isBlank(userId)) {
      userId = "default_user";
    }
    String response = krishnaService.getRecommendationFromKrishna(applicationId, userId);
    MDC.put(RESPONSE_TIME, String.valueOf(
        System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP_MS))));
    log.info("Rest GET API /event/chatbot/{}/krishna processed", applicationId);
    return ResponseEntity.ok(response);
  }

  @SneakyThrows
  @GetMapping(path = "/{applicationId}/integration", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ChatbotIntegrationForUI> getChatBotIntegration(
      @PathVariable("applicationId") String applicationId) {
    MDC.put(PROJECT_ID, applicationId);
    log.debug("Rest GET API /event/chatbot/{}/integration triggered", applicationId);
    ChatbotIntegrationForUI response = chatBotService.getChatBotIntegration(applicationId);
    MDC.put(RESPONSE_TIME, String.valueOf(
        System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP_MS))));
    log.info("Rest GET API /event/chatbot/{}/integration processed", applicationId);
    return ResponseEntity.ok(response);
  }

  @GetMapping(path = "/{applicationId}/promotions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<DataSet>> getAllPromotions(
      @PathVariable("applicationId") String applicationId) {
    MDC.put(PROJECT_ID, applicationId);
    log.debug("Rest GET API /event/chatbot/{}/promotions triggered", applicationId);
    List<DataSet> response = chatBotService.getPromotions(applicationId);
    MDC.put(RESPONSE_TIME, String.valueOf(
        System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP_MS))));
    log.info("Rest GET API /event/chatbot/{}/promotions processed", applicationId);
    return ResponseEntity.ok(response);
  }

  @GetMapping(path = "/{applicationId}/quick_links", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<DataSet>> getAllQuickLinks(
      @PathVariable("applicationId") String applicationId) {
    MDC.put(PROJECT_ID, applicationId);
    log.debug("Rest GET API /event/chatbot/{}/quick_links triggered", applicationId);
    List<DataSet> response = chatBotService.getQuickLinks(applicationId);
    MDC.put(RESPONSE_TIME, String.valueOf(
        System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP_MS))));
    log.info("Rest GET API /event/chatbot/{}/quick_links processed", applicationId);
    return ResponseEntity.ok(response);
  }

  @SneakyThrows
  @GetMapping(path = "/{applicationId}/preIntegration", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ChatPreIntegration> getChatBotPreIntegration(
      @PathVariable("applicationId") String applicationId) {
    MDC.put(PROJECT_ID, applicationId);
    log.debug("Rest GET API /event/chatbot/{}/preIntegration triggered", applicationId);
    ApplicationIntegration integration =
        integrationService.getApplicationIntegration(applicationId);
    MDC.put(RESPONSE_TIME, String.valueOf(
        System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP_MS))));
    log.info("Rest GET API /event/chatbot/{}/preIntegration processed", applicationId);
    return ResponseEntity.ok(ChatbotIntegrationForUI.from((ChatbotIntegration) integrationService
        .getChannelIntegration(applicationId, Channel.HTML5, integration))
        .getChatBotPreIntegration());

  }

  @GetMapping(path = "/{applicationId}/quick_reply", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<DataSet>> getQuickReplies(
      @PathVariable("applicationId") String applicationId) {
    MDC.put(PROJECT_ID, applicationId);
    log.debug("Rest GET API /event/chatbot/{}/quick_reply triggered", applicationId);
    List<DataSet> response = chatBotService.getQuickReplies(applicationId);
    MDC.put(RESPONSE_TIME, String.valueOf(
        System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP_MS))));
    log.info("Rest GET API /event/chatbot/{}/quick_reply precessed", applicationId);
    return ResponseEntity.ok(response);
  }

  @PostMapping(path = "/{applicationId}/history", produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<Map<String, Object>>> getChatHistory(
      @PathVariable("applicationId") String applicationId,
      @RequestParam(value = "page_no", required = false) Integer pageNo,
      @RequestParam(value = "items_per_page", required = false) Integer itemsPerPage,
      @RequestBody Map<String, Object> filter) {
    MDC.put(PROJECT_ID, applicationId);
    Validate.notBlank(applicationId);
    log.debug("Rest POST API /event/chatbot/{}/history triggered", applicationId);
    List<Map<String, Object>> response =
        analyticsService.fetchTurnLogsFromAnalytics(applicationId, pageNo, itemsPerPage, filter);
    MDC.put(RESPONSE_TIME, String.valueOf(
        System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP_MS))));
    log.info("Rest POST API /event/chatbot/{}/history precessed", applicationId);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping(path = "/{applicationId}/history")
  public ResponseEntity<Void> deleteChatHistory(@PathVariable("applicationId") String applicationId,
      @RequestParam("user_id") String userId) {
    MDC.put(PROJECT_ID, applicationId);
    Validate.notBlank(applicationId);
    log.debug("Rest DELETE API /event/chatbot/{}/history triggered", applicationId);
    analyticsService.deleteTurnLogsChatbot(applicationId, userId);
    MDC.put(RESPONSE_TIME, String.valueOf(
        System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP_MS))));
    log.info("Rest DELETE API /event/chatbot/{}/history precessed", applicationId);
    return ResponseEntity.ok().build();
  }

  @PostMapping(path = "/{applicationId}/autosuggest", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<String>> getAutoSuggestion(
      @PathVariable("applicationId") String applicationId, @RequestParam("q") String text,
      @RequestBody Map<String, Object> filter) throws BadRequestException {
    MDC.put(PROJECT_ID, applicationId);
    log.debug("Rest API GET /event/chatbot/autosuggest triggered", applicationId);
    List<String> responses = chatBotService.getAutoCompleteSuggestions(applicationId, text,
        (Boolean) filter.getOrDefault("showSerenResult", true));
    MDC.put(RESPONSE_TIME, String.valueOf(
        System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP_MS))));
    log.info("Rest API GET /event/widget/{}/autosuggest processed", applicationId);
    return ResponseEntity.ok(responses);
  }

  @GetMapping(path = "/{applicationId}/search", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getAllAnswers(@PathVariable("applicationId") String applicationId,
      @RequestHeader("X-USER-ID") String userId, @RequestHeader("X-SESSION-ID") String sessionId,
      @RequestHeader("Channel") String channel, @RequestParam("q") String query,
      @RequestParam("log_me") Boolean logEvent) {
    MDC.put(PROJECT_ID, applicationId);
    log.debug("Rest GET API /event/chatbot/searchInChat triggered.", applicationId);
    Object response =
        chatBotService.getAllAnswers(applicationId, userId, sessionId, query, channel, logEvent);
    MDC.put(RESPONSE_TIME, String.valueOf(
        System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP_MS))));
    log.info("Rest GET API /event/chatbot/searchInChat processed.", applicationId);
    return ResponseEntity.ok(response);
  }

}
