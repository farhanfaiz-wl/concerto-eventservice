package ai.concerto.event.service;

import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Source;
import ai.concerto.event.exchange.BotResponse;
import ai.concerto.event.exchange.UserDetails;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.utils.Constants.HttpConstants;
import ai.concerto.event.utils.Constants.MdcConstants;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class SereneService {

  @Data
  public static class SereneAutoCompleteResult {
    @JsonProperty("_id")
    public String id;
    @JsonProperty("_score")
    public Double score;
    @JsonProperty("complete")
    public String complete;
  }

  @Autowired
  private RestUtils restUtils;

  @Autowired
  @Qualifier("withOutEncoding")
  private RestTemplate restTemplate;

  @Autowired
  private ObjectMapper snakeMaster;

  @Autowired
  private RedisTemplate redisTemplate;

  @Autowired
  private ServiceDetails serviceDetails;

  @Autowired
  private AnalyticsService analyticsService;

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Value("${spring.redis.stream.analytics.turn_logs.key}")
  private String turnLogsStreamKey;

  private static final String X_PROJECT_ID = "X-Project-Id";
  private static final String X_APIKEY = "X-APIKEY";
  private static final String SCHEMA_SEARCH_URI_FORMAT = "%s/projects/%s/schemas/%s/search";
  private static final String UNIVERSAL_SEARCH_URI_FORMAT = "%s/projects/%s/search";
  private static final String SCHEMA_AUTOCOMPLETE_URI_FORMAT =
      "%s/projects/%s/schemas/%s/autocomplete";
  private static final String UNIVERSAL_AUTOCOMPLETE_URI_FORMAT = "%s/projects/%s/autocomplete";
  private static final String SERENE_AUTOCOMPLETE_URI_FORMAT = "%s/search/%s/autocomplete";

  public String fetchSchemaSearch(String projectId, String userId, String query, String schemaId,
      String request, String sort, Boolean includeQaMatches, Integer pageNo, Integer pageSize) {
    String uri = String.format(SCHEMA_SEARCH_URI_FORMAT, serviceDetails.getSeren().getUri(),
        projectId, schemaId);
    String searchResult = fetchSearchResult(projectId, uri, query, request, null, pageSize, sort,
        null, includeQaMatches, pageNo);

    try {
      logSearchResult(projectId, userId, query, searchResult);
    } catch (Exception e) {
      log.error("Unable to publish log data for schema search", e);
      analyticsStreamPublisher.publishErrorLogssAnalytics(getBotResponse(projectId, userId),
          e.getMessage());
    }
    return searchResult;
  }

  public String fetchUniversalSearchResult(String projectId, String userId, String query,
      Integer pageNo, Integer pageSize, String sort, Boolean includeQaMatches, String schemas) {

    String uri =
        String.format(UNIVERSAL_SEARCH_URI_FORMAT, serviceDetails.getSeren().getUri(), projectId);
    String searchResult = fetchSearchResult(projectId, uri, query, null, schemas, pageSize, sort,
        null, includeQaMatches, pageNo);

    try {
      logSearchResult(projectId, userId, query, searchResult);
    } catch (Exception e) {
      log.error("Unable to publish log data for search", e);
      analyticsStreamPublisher.publishErrorLogssAnalytics(getBotResponse(projectId, userId),
          e.getMessage());
    }
    return searchResult;
  }

  public String fetchUniversalAutoComplete(String projectId, String query, Integer limit) {

    String uri = String.format(UNIVERSAL_AUTOCOMPLETE_URI_FORMAT,
        serviceDetails.getSeren().getUri(), projectId);
    return fetchSearchResult(projectId, uri, query, null, null, null, null, limit, null, null);
  }

  public String fetchSchemaAutoComplete(String projectId, String schemaId, String query,
      Integer limit) {
    String uri = String.format(SCHEMA_AUTOCOMPLETE_URI_FORMAT, serviceDetails.getSeren().getUri(),
        projectId, schemaId);
    return fetchSearchResult(projectId, uri, query, null, null, null, null, limit, null, null);
  }

  private void logSearchResult(String projectId, String userId, String query, String searchResult)
      throws Exception {
    List<Map<String, Object>> hits = new ArrayList<>();
    if (searchResult.startsWith("[")) {
      List<Map<String, Object>> results = snakeMaster.readValue(searchResult, snakeMaster
          .getTypeFactory().constructType(new TypeReference<List<Map<String, Object>>>() {}));
      results.forEach(result -> hits.addAll((List<Map<String, Object>>) result.get("hits")));
    } else {
      Map<String, Object> results = snakeMaster.readValue(searchResult,
          snakeMaster.getTypeFactory().constructType(new TypeReference<Map<String, Object>>() {}));
      hits.addAll((List<Map<String, Object>>) results.get("hits"));
    }

    DERequest deRequest = new DERequest();
    deRequest.setTurnId(UUID.randomUUID().toString());
    deRequest.setProjectId(projectId);
    deRequest.setUserId(userId);
    deRequest.setSource(Source.search_api);
    deRequest.setUserInputLast(query);

    UserDetails userDetails = analyticsService.getUserDetails(projectId, deRequest.getUserId(),
        deRequest.getSource().name(), deRequest.getTenantId(), deRequest);
    deRequest.setUniversalUserId(userDetails.getUserProfile().getUniversalUserId());
    deRequest.setTicketId(userDetails.getTicket().getId());
    deRequest.setUserProfile(userDetails.getUserProfile());

    Map<String, Object> dialog = new HashMap<>();

    Map<String, Object> log = new HashMap<>();
    log.put("client_info", deRequest);
    log.put("dialog", dialog);

    Map<String, String> messageToPublish = new HashMap<>();
    messageToPublish.put("turn_id", deRequest.getTurnId());
    messageToPublish.put("log", snakeMaster.writeValueAsString(log));

    MapRecord<String, String, String> turnLogRecord =
        StreamRecords.mapBacked(messageToPublish).withStreamKey(turnLogsStreamKey);

    redisTemplate.opsForStream().add(turnLogRecord);
  }

  @SneakyThrows
  private String fetchSearchResult(String projectId, String uri, String query, String request,
      String schemas, Integer pageSize, String sort, Integer limit, Boolean includeQaMatches,
      Integer pageNo) {
    try {

      UriComponentsBuilder requestUriBuilder = UriComponentsBuilder.fromHttpUrl(uri);

      if (StringUtils.hasText(query)) {
        requestUriBuilder.queryParam("q", URLEncoder.encode(query, "UTF-8"));
      }
      if (StringUtils.hasText(schemas)) {
        requestUriBuilder.queryParam("schemas", schemas);
      }
      if (!ObjectUtils.isEmpty(pageNo)) {
        requestUriBuilder.queryParam("page_number", pageNo);
      }
      if (!ObjectUtils.isEmpty(pageSize)) {
        requestUriBuilder.queryParam("page_size", pageSize);
      }
      if (StringUtils.hasText(sort)) {
        requestUriBuilder.queryParam("sort", URLEncoder.encode(sort, "UTF-8"));
      }
      if (!ObjectUtils.isEmpty(limit)) {
        requestUriBuilder.queryParam("limit", limit);
      }

      if (!ObjectUtils.isEmpty(includeQaMatches)) {
        requestUriBuilder.queryParam("include_qa_matches", includeQaMatches);
      }

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      if (!ObjectUtils.isEmpty(MDC.get(MdcConstants.CORRELATION_ID)))
        headers.set(HttpConstants.HEADER_KEY_CORRELATION_ID, MDC.get(MdcConstants.CORRELATION_ID));
      headers.set(X_APIKEY, serviceDetails.getSeren().getAppKey());
      headers.set(X_PROJECT_ID, projectId);
      HttpEntity<?> entity = new HttpEntity<>(headers);
      ResponseEntity<String> response = restTemplate
          .exchange(requestUriBuilder.build().toUriString(), HttpMethod.POST, entity, String.class);

      return response.getBody();
    } catch (Exception e) {
      log.error("Unable to fetch the search from seren {}", e);
      throw e;
    }

  }

  public List<SereneAutoCompleteResult> getAutoCompleteResults(String applicationId, String text) {
    StringBuilder uriBuf = new StringBuilder(String.format(SERENE_AUTOCOMPLETE_URI_FORMAT,
        serviceDetails.getSeren().getUri(), applicationId));
    if (StringUtils.hasText(text)) {
      try {
        String encText = URLEncoder.encode(text, Charset.defaultCharset().name());
        uriBuf.append("?q=").append(encText).append("&").append("limit=5");
      } catch (UnsupportedEncodingException e) {
        log.error("Unable to encode search text : {}", text, e);
      }
    }

    Properties headerProperties = new Properties();
    headerProperties.put(X_APIKEY, serviceDetails.getSeren().getAppKey());
    headerProperties.put(X_PROJECT_ID, applicationId);

    try {
      log.debug("getting seren autocomplete Results : {}", uriBuf.toString());
      String response =
          restUtils.postRequest(uriBuf.toString(), null, headerProperties, String.class);
      return snakeMaster.readValue(response,
          new TypeReference<List<SereneAutoCompleteResult>>() {});
    } catch (Exception e) {
      log.error("Unable to get seren autocomplete results", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Unable to process " + uriBuf.toString());
    }
  }

  private BotResponse getBotResponse(String projectId, String userId) {
    BotResponse botResponse = new BotResponse();
    botResponse.setProjectId(projectId);
    botResponse.setUserId(userId);
    botResponse.setSource(Source.search_widget.name());
    return botResponse;
  }

}
