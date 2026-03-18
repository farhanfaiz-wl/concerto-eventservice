package ai.concerto.event.publisher;

import ai.concerto.event.dto.ChatClientEvent;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.exchange.BotResponse;
import ai.concerto.event.exchange.ErrorLogStreamResponse;
import ai.concerto.event.exchange.ErrorLogStreamResponse.ErrorLog;
import ai.concerto.event.exchange.Tenant;
import ai.concerto.event.service.FalconService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Slf4j
@Service
public class AnalyticsStreamPublisher {

  @Autowired
  private RedisTemplate redisTemplate;

  @Autowired
  private FalconService falconService;

  @Autowired
  private ObjectMapper snakeCaseMapper;

  @Value("${spring.redis.stream.analytics.agent_msgs.key}")
  private String agentMsgStreamKey;

  @Value("${spring.redis.stream.analytics.chat_event.key}")
  private String chatEventStreamKey;

  @Value("${spring.redis.stream.analytics.turn_logs.key}")
  private String turnLogsStreamKey;

  @Value("${spring.redis.stream.analytics.form_slot.key}")
  private String formSlotStreamKey;

  @Value("${spring.redis.stream.analytics.quiz.key}")
  private String quizStreamKey;

  @Value("${spring.redis.stream.analytics.error_logs.key}")
  private String errorLogsKey;

  @Value("${spring.redis.stream.analytics.ticket.key}")
  private String ticketSessionKey;

  public void publishAgentMessage(String agentReply) {
    MapRecord<String, String, String> agentReplyRecord = StreamRecords
        .mapBacked(Collections.singletonMap("data", agentReply)).withStreamKey(agentMsgStreamKey);

    redisTemplate.opsForStream().add(agentReplyRecord);
  }

  public void publishChatClientEvent(ChatClientEvent event) {
    Map<String, String> eventMap =
        snakeCaseMapper.convertValue(event, new TypeReference<Map<String, String>>() {});
    MapRecord<String, String, String> eventRecord =
        StreamRecords.mapBacked(eventMap).withStreamKey(chatEventStreamKey);

    redisTemplate.opsForStream().add(eventRecord);

  }

  public void publishTurnLog(DERequest deRequest) {
    Map<String, Object> clientInfo = Collections.singletonMap("client_info", deRequest);
    Map<String, String> messageToPublish = new HashMap<>();
    messageToPublish.put("turn_id", deRequest.getTurnId());
    try {
      messageToPublish.put("log", snakeCaseMapper.writeValueAsString(clientInfo));
    } catch (JsonProcessingException e) {
      log.error("Unable to parse deRequest", e);
      return;
    }

    MapRecord<String, String, String> turnLogRecord =
        StreamRecords.mapBacked(messageToPublish).withStreamKey(turnLogsStreamKey);
    redisTemplate.opsForStream().add(turnLogRecord);
  }

  public void publishTurnLogWithDataSet(DERequest deRequest, String dataToBePublish) {

    Map<String, Object> clientInfo = new HashMap<>();
    clientInfo.put("client_info", deRequest);
    clientInfo.put("data_set", dataToBePublish);
    Map<String, String> messageToPublish = new HashMap<>();
    messageToPublish.put("turn_id", deRequest.getTurnId());
    try {
      messageToPublish.put("log", snakeCaseMapper.writeValueAsString(clientInfo));
    } catch (JsonProcessingException e) {
      log.error("Unable to parse deRequest", e);
      return;
    }
    MapRecord<String, String, String> turnLogRecord =
        StreamRecords.mapBacked(messageToPublish).withStreamKey(turnLogsStreamKey);
    redisTemplate.opsForStream().add(turnLogRecord);
  }

  public void publishAbandonedResponsesAnalytics(String projectId, String channel, String userId,
      String universalUserId, String sessionId, Boolean inForm, Boolean inQuiz) {
    Map<String, String> message = new HashMap<>();
    message.put("project_id", projectId);
    message.put("user_id", userId);
    message.put("universal_user_id", universalUserId);
    message.put("channel", channel);
    message.put("session_id", sessionId);
    message.put("status", "abandoned");
    message.put("abandoned_reason", "session expired by event service");
    MapRecord<String, String, String> formSlotRecord =
        StreamRecords.mapBacked(message).withStreamKey(formSlotStreamKey);
    MapRecord<String, String, String> quizRecord =
        StreamRecords.mapBacked(message).withStreamKey(quizStreamKey);
    MapRecord<String, String, String> ticketSession =
        StreamRecords.mapBacked(message).withStreamKey(ticketSessionKey);

    redisTemplate.opsForStream().add(ticketSession);
    if (Boolean.TRUE.equals(inForm)) {
      redisTemplate.opsForStream().add(formSlotRecord);
    }
    if (Boolean.TRUE.equals(inQuiz)) {
      redisTemplate.opsForStream().add(quizRecord);
    }

  }

  @SuppressWarnings("unchecked")
  public void publishErrorLogssAnalytics(BotResponse response, String error) {
    ErrorLogStreamResponse errorLogResponse = new ErrorLogStreamResponse();
    ErrorLog errorLog = new ErrorLog();

    try {
      errorLog.setProjectId(response.getProjectId());
      errorLog.setUserId(response.getUserId());
      errorLog.setUniversalUserId(response.getUniversalUserId());
      errorLog.setSource(response.getSource());
      errorLog.setSessionId(response.getSessionId());
      errorLog.setTurnId(response.getTurnId());
      errorLog.setLevel("ERROR");
      errorLog.setMessage(error);
      errorLog.setCategory("CHANNEL INTEGRATION ERROR");
      errorLog.setComponentName("eventservice");

      Tenant tenant = falconService.getTenantByProjectId(response.getProjectId());
      if (!ObjectUtils.isEmpty(tenant)) {
        errorLog.setTenantId(tenant.getId());
        errorLog.setTenantName(tenant.getName());
      }

      errorLogResponse.setErrorLog(snakeCaseMapper.writeValueAsString(errorLog));

      MapRecord<String, String, String> errorLogsRecord =
          StreamRecords.mapBacked(snakeCaseMapper.convertValue(errorLogResponse, Map.class))
              .withStreamKey(errorLogsKey);
      redisTemplate.opsForStream().add(errorLogsRecord);
    } catch (Exception e) {
      log.error("error in sending error logs to analytics {}", e.getMessage());
    }

  }

}
