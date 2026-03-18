package ai.concerto.event.service;

import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.dto.NluModel;
import ai.concerto.event.dto.UserRequestEvent;
import ai.concerto.event.enums.DataSetSchema;
import ai.concerto.event.enums.Source;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.ChatbotIntegrationForUI;
import ai.concerto.event.exchange.ChatBotSearchResponse;
import ai.concerto.event.exchange.DataSet;
import ai.concerto.event.exchange.UserDetails;
import ai.concerto.event.handler.request.WidgetRequestHandler;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.render.WidgetRenderer;
import ai.concerto.event.service.NLUService.NLUAutoCompleteResult;
import ai.concerto.event.service.SereneService.SereneAutoCompleteResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.util.BadRequestException;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class ChatBotService {

  @Autowired
  private FalconService falconService;

  @Autowired
  private IntegrationService integrationService;

  @Autowired
  private NLUService nluService;

  @Autowired
  private SereneService sereneService;

  @Autowired
  private AnalyticsService analyticsService;

  @Autowired
  private WidgetRequestHandler widgetRequestHandler;

  @Autowired
  private WidgetRenderer widgetRenderer;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private DEService deService;

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  @Autowired
  private SimpUserRegistry simpUserRegistry;

  @Autowired
  private UserRequestStreamService userRequestStreamService;

  private static final String QUICK_REPLY_CHATBOT_FILTER = "data.channel=CHATBOT";

  @SuppressWarnings("unchecked")
  public void processChatbotMessage(Principal principal, String message, String sessionId)
      throws Exception {

    // sending the user message to other tabs of same user if present excluding the current session
    SimpUser user = simpUserRegistry.getUser(principal.getName());
    if (!ObjectUtils.isEmpty(user)) {
      List<String> sessionIds = user.getSessions().stream().map(SimpSession::getId).toList();

      sessionIds.stream().filter(sessId -> !sessId.equals(sessionId)).forEach(sessId -> {
        SimpMessageHeaderAccessor headerAccessor =
            SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessId);
        headerAccessor.setLeaveMutable(true);
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/usermessage", message,
            headerAccessor.getMessageHeaders());
      });
    }

    // processing the user message
    UserRequestEvent userRequestEvent = objectMapper.readValue(message, UserRequestEvent.class);
    String applicationId = userRequestEvent.getApplicationId();
    Map<String, String> request = objectMapper.readValue(userRequestEvent.getRequest(), Map.class);
    request.put("web_socket_user_name", principal.getName());

    userRequestStreamService.processUserRequest(applicationId,
        objectMapper.readValue(objectMapper.writeValueAsString(request), Object.class),
        userRequestEvent.getChannel(), userRequestEvent.getVendor());
  }

  public List<DataSet> getPromotions(String applicationId) {
    if (StringUtils.hasText(applicationId)) {
      return falconService.getDataSets(applicationId, DataSetSchema.PROMOTION_V1, null);
    } else {
      log.info("Null or empty project id");
      return Collections.emptyList();
    }
  }

  public List<DataSet> getQuickLinks(String applicationId) {
    if (StringUtils.hasText(applicationId)) {
      return falconService.getDataSets(applicationId, DataSetSchema.QUICK_LINK_V1, null);
    } else {
      log.info("Null or empty project id");
      return Collections.emptyList();
    }
  }

  public List<DataSet> getQuickReplies(String applicationId) {
    log.info("Quick Link and Replies received from falcon.");
    return getFilteredDataSets(applicationId, DataSetSchema.QUICK_REPLY_V1,
        QUICK_REPLY_CHATBOT_FILTER);
  }

  private List<DataSet> getFilteredDataSets(String applicationId, DataSetSchema schema,
      String queryFilter) {

    List<DataSet> dataSets = new ArrayList<>();

    Optional<List<DataSet>> filteredDataSets =
        Optional.ofNullable(falconService.getDataSets(applicationId, schema, queryFilter));

    if (filteredDataSets.isPresent()) {
      dataSets = filteredDataSets.get().stream().toList();
    }
    return dataSets;
  }

  public List<String> getAutoCompleteSuggestions(String applicationId, String text,
      Boolean showSerenResult) throws BadRequestException {

    List<String> results = new ArrayList<>();
    try {
      List<NLUAutoCompleteResult> nluSearchResult =
          nluService.getAutoCompleteResults(applicationId, text);
      if (!ObjectUtils.isEmpty(nluSearchResult)) {
        nluSearchResult.stream().map(NLUAutoCompleteResult::getComplete).forEach(results::add);
      }
      if (Boolean.TRUE.equals(showSerenResult)) {
        List<SereneAutoCompleteResult> sereneSearchResults =
            sereneService.getAutoCompleteResults(applicationId, text);
        if (!ObjectUtils.isEmpty(sereneSearchResults)) {
          sereneSearchResults.stream().map(SereneAutoCompleteResult::getComplete)
              .forEach(results::add);
        }
      }
    } catch (Exception e) {
      log.error("Failed  to fetch auto complete results", e);
    }
    return results;
  }

  public Object getAllAnswers(String applicationId, String userId, String sessionId, String query,
      String channel, Boolean logEvent) {

    ChatBotSearchResponse chatBotSearchResponse = new ChatBotSearchResponse();
    try {
      DERequest deRequest =
          widgetRequestHandler.getDeRequest(applicationId, userId, sessionId, query);

      // setting universal_user_id
      if (StringUtils.hasText(query)) {

        if (ObjectUtils.isEmpty(deRequest.getSource()))
          deRequest.setSource(Source.answer_widget);

        UserDetails userDetails =
            analyticsService.getUserDetails(applicationId, deRequest.getUserId(),
                deRequest.getSource().name(), deRequest.getTenantId(), deRequest);

        if (!ObjectUtils.isEmpty(logEvent)) {
          deRequest.setLogMe(logEvent);
        }

        deRequest.setUniversalUserId(userDetails.getUserProfile().getUniversalUserId());
        deRequest.setTicketId(userDetails.getTicket().getId());
        deRequest.setUserProfile(userDetails.getUserProfile());
        if (Objects.equals(channel, "search_widget")) {
          deRequest.setSendSearchResults(true);
          deRequest.setSource(Source.search_widget);
          DEBotResponse deResponse = deService.postDeRequest(deRequest);
          chatBotSearchResponse =
              (ChatBotSearchResponse) widgetRenderer.setSearchResult(deResponse);
        }
        setQAResultFromNlp(applicationId, chatBotSearchResponse, deRequest, query);

      } else {
        chatBotSearchResponse.setBotRepliesQAResults(Collections.emptyList());
      }
      chatBotSearchResponse.setTurnId(deRequest.getTurnId());
      MDC.put("response_time", String
          .valueOf(System.currentTimeMillis() - Long.parseLong(MDC.get("request_timestamp_ms"))));
      log.info("SuccessFully generated chatbot search response");
    } catch (Exception ex) {
      log.error("Error occurred while fetching search result and QA {}", ex);
    }
    return chatBotSearchResponse;
  }

  @SneakyThrows
  private void setQAResultFromNlp(String applicationId, ChatBotSearchResponse chatBotSearchResponse,
      DERequest deRequest, String query) {

    Optional<NluModel> nluModel = falconService.getNluModelByAppId(applicationId);
    if (nluModel.isPresent()) {
      String methodId = nluModel.get().getMethodId();
      List<DataSet> nluSearchResult =
          nluService.getAnswersFromNLP(methodId, applicationId, query, 10, null);
      if (!Objects.equals(deRequest.getSource(), Source.search_widget)) {
        DataSet dataSet = nluSearchResult.get(0);
        if (!ObjectUtils.isEmpty(dataSet.getData().get("query"))) {
          List<Map<String, String>> queries =
              (List<Map<String, String>>) dataSet.getData().get("query");
          if (!ObjectUtils.isEmpty(queries.get(0))
              && !ObjectUtils.isEmpty(queries.get(0).get("text"))) {
            deRequest.setUserInputLast(queries.get(0).get("text"));
            DEBotResponse deResponse = deService.postDeRequest(deRequest);
            widgetRenderer.setRecommendationInDataSet(deResponse, nluSearchResult.get(0));
          }
        }
      }
      String dataToBePublish = objectMapper.writeValueAsString(nluSearchResult);
      if (deRequest.isLogMe()) {
        CompletableFuture.supplyAsync(() -> {
          analyticsStreamPublisher.publishTurnLogWithDataSet(deRequest, dataToBePublish);
          return 1;
        });
      }
      chatBotSearchResponse.setBotRepliesQAResults(nluSearchResult);
    } else {
      chatBotSearchResponse.setBotRepliesQAResults(Collections.emptyList());
    }
  }

  public ChatbotIntegrationForUI getChatBotIntegration(String applicationId) {
    Optional<ApplicationIntegration> integration =
        falconService.getApplicationIntegration(applicationId);
    ChatbotIntegrationForUI chatbotIntegrationForUI = null;
    if (integration.isPresent()
        && !ObjectUtils.isEmpty(integration.get().getChatbotIntegration())) {
      chatbotIntegrationForUI =
          ChatbotIntegrationForUI.from(integration.get().getChatbotIntegration());
      chatbotIntegrationForUI
          .setLanguage((String) integration.get().getProjectSettings().get("project_lang"));
      chatbotIntegrationForUI.setVoiceSetting(
          (Map<String, String>) integration.get().getProjectSettings().get("voice_setting"));
    }
    return chatbotIntegrationForUI;
  }
}
