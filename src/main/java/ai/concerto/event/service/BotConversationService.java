package ai.concerto.event.service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.BotConversationDetails;
import ai.concerto.event.exchange.BotConversationEndResponse;
import ai.concerto.event.exchange.BotConversationRequest;
import ai.concerto.event.exchange.BotConversationResponse;
import ai.concerto.event.exchange.BotConversationStartRequest;
import ai.concerto.event.exchange.BotConversationStartResponse;
import ai.concerto.event.exchange.BotMessage;
import ai.concerto.event.handler.response.StatusResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BotConversationService {

  @Autowired
  private FalconService falconService;

  @Autowired
  private ObjectMapper snakeMaster;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Autowired
  private UserRequestService userRequestService;

  @Autowired
  SessionService sessionService;

  public static final int BOT_CONVERSATIONS_IDLE_TIMEOUT_MIN_VALUE = 60;

  private static final String BOT_SESSION_KEY_FORMATTER = "BOT_SESSION_%s";

  public BotConversationStartResponse newConversation(BotConversationStartRequest request)
      throws ResponseStatusException {
    if (request.getIdleTimeoutS() < BOT_CONVERSATIONS_IDLE_TIMEOUT_MIN_VALUE) {
      log.error("Received idle timeout {} is smaller than minimum timeout {}",
          request.getIdleTimeoutS(), BOT_CONVERSATIONS_IDLE_TIMEOUT_MIN_VALUE);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Idle timeout should be greater than " + BOT_CONVERSATIONS_IDLE_TIMEOUT_MIN_VALUE);
    }

    Optional<ApplicationIntegration> applicationIntegration =
        falconService.getApplicationIntegration(request.getProjectId());
    if (!applicationIntegration.isPresent()) {
      log.error("Project integrations not found for projectId: {}", request.getProjectId());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    String conversationId = UUID.randomUUID().toString();
    BotConversationDetails conversation = new BotConversationDetails(conversationId, "created",
        request.getUser(), request.getProjectId(), request.getChannel(), request.getVendor(),
        request.getIdleTimeoutS(), System.currentTimeMillis(), System.currentTimeMillis());
    try {
      String conversationStr = snakeMaster.writeValueAsString(conversation);
      redisTemplate.opsForValue().set(String.format(BOT_SESSION_KEY_FORMATTER, conversationId),
          conversationStr, request.getIdleTimeoutS(), TimeUnit.SECONDS);
    } catch (JsonProcessingException e) {
      log.error("Unable to serialize conversation details", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return BotConversationStartResponse.from(conversationId);
  }

  public BotConversationEndResponse deleteConversation(String conversationId) {

    String conversationKey = String.format(BOT_SESSION_KEY_FORMATTER, conversationId);
    String conversationStr = snakeMaster.convertValue(
        redisTemplate.opsForValue().get(conversationKey), new TypeReference<String>() {});
    if (ObjectUtils.isEmpty(conversationStr))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);

    redisTemplate.delete(conversationKey);
    return new BotConversationEndResponse(true);
  }

  public BotConversationResponse generateBotResponse(String conversationId,
      BotConversationRequest request) throws Exception {

    String conversationKey = String.format(BOT_SESSION_KEY_FORMATTER, conversationId);
    String conversationStr = snakeMaster.convertValue(
        redisTemplate.opsForValue().get(conversationKey), new TypeReference<String>() {});
    if (ObjectUtils.isEmpty(conversationStr))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);

    String vendor = null;
    BotConversationDetails conversation;

    try {
      conversation = snakeMaster.readValue(conversationStr, snakeMaster.getTypeFactory()
          .constructType(new TypeReference<BotConversationDetails>() {}));
      if (!ObjectUtils.isEmpty(conversation.getVendor())) {
        vendor = conversation.getVendor().toUpperCase();
      }
      conversation.setConversationStatus("ongoing");
      conversation.setUpdatedAt(System.currentTimeMillis());
      redisTemplate.opsForValue().set(conversationKey, snakeMaster.writeValueAsString(conversation),
          conversation.getIdleTimeoutS(), TimeUnit.SECONDS);
    } catch (JsonProcessingException e) {
      log.error("Unable to deserialize conversation details", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    DERequest deRequest = new DERequest();

    deRequest.setUserId(conversation.getUser().getId());
    deRequest.setUserFirstName(conversation.getUser().getFirstName());
    deRequest.setUserLastName(conversation.getUser().getLastName());
    deRequest.setUserInputLast(request.getMessage().getText());


    Object botReplyStr = userRequestService.processUserRequest(conversation.getProjectId(),
        deRequest, Channel.API, vendor);

    if (ObjectUtils.isEmpty(botReplyStr)) {
      log.error("Unable to get/process dgi response for projectId: {}",
          conversation.getProjectId());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Unable to generate bot reply");
    }

    String messageId = UUID.randomUUID().toString();
    BotMessage botReplyMessage;
    botReplyMessage = snakeMaster.convertValue(botReplyStr, BotMessage.class);

    BotConversationResponse botConversationResponse = new BotConversationResponse();
    botConversationResponse.setId(messageId);
    botConversationResponse.setMessage(botReplyMessage);
    botConversationResponse
        .setStatus(new StatusResponse(200, "success", "Successfully fetched bot reply"));

    return botConversationResponse;
  }
}
