package ai.concerto.event.service;

import java.util.Optional;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.exception.RequestHandlerException;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.ServiceProvider;
import ai.concerto.event.exchange.ApplicationIntegration.SmsIntegration;
import ai.concerto.event.exchange.BotSmsResponse;
import ai.concerto.event.exchange.Project;
import ai.concerto.event.exchange.SmsUserRequest;
import ai.concerto.event.handler.response.SmsTwilioResponseHandler;
import ai.concerto.event.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SmsService {

  private static final String SMS_RETRY_KEY_FORMATTER = "evs::sms_retry:%s";
  private static final String RIPPLES_SMSBOT_URI_FORMAT = "%s/projects/%s/twilio/smsbot";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private FalconService falconService;

  @Autowired
  private IntegrationService integrationService;

  @Autowired
  private SmsTwilioResponseHandler smsTwilioResponseHandler;

  @Autowired
  private RedisTemplate redisTemplate;

  @Autowired
  private ServiceDetails serviceDetails;

  @Autowired
  private RestUtils restUtils;

  @SuppressWarnings("unchecked")
  public void handleAgentSmsFailure(String applicationId, MultiValueMap<String, String> params) {

    String status = params.getFirst("MessageStatus");
    String messageId = params.getFirst("MessageSid");
    // get smsResponse from cache if present
    Object smsRetryResponse =
        redisTemplate.opsForValue().get(String.format(SMS_RETRY_KEY_FORMATTER, messageId));

    try {
      // try the retry logic if the message status is undelivered or failed
      if (!ObjectUtils.isEmpty(smsRetryResponse) && StringUtils.hasText(status)
          && (status.equals("undelivered") || status.equals("failed"))) {
        BotSmsResponse smsResponse =
            objectMapper.readValue((String) smsRetryResponse, BotSmsResponse.class);

        if (!ObjectUtils.isEmpty(smsResponse) && smsResponse.getRetryCount() < 1) {
          // delete the cached response as its no longer needed
          redisTemplate.delete(String.format(SMS_RETRY_KEY_FORMATTER, messageId));
          Project project = falconService.getProjectById(applicationId);
          log.error(String.format("Agent SMS undelivered -> %s/%s -> %s", smsResponse.getTicketId(),
              project.getName(), params.getFirst("ErrorMessage")));
        } else {
          // delete the existing cached response
          redisTemplate.delete(String.format(SMS_RETRY_KEY_FORMATTER, messageId));

          ApplicationIntegration integration =
              integrationService.getApplicationIntegration(applicationId);
          SmsIntegration channelIntegration = (SmsIntegration) integrationService
              .getChannelIntegration(applicationId, Channel.SMS, integration);
          Optional<ServiceProvider> serviceProvider =
              channelIntegration.getServiceProvidersList().stream().findFirst();
          if (!serviceProvider.isPresent())
            throw new RequestHandlerException("Twilio provider not found for sms integration!");

          BotSmsResponse botSmsResponse = new BotSmsResponse();
          botSmsResponse.setAuthId(serviceProvider.get().getProviderDetails().getAccountId());
          botSmsResponse.setAuthToken(serviceProvider.get().getProviderDetails().getAuthToken());
          botSmsResponse.setFrom(channelIntegration.getPhoneNumber());
          botSmsResponse.setTo(params.getFirst("To"));
          botSmsResponse.setMessage(smsResponse.getMessage());
          botSmsResponse.setProjectId(applicationId);
          botSmsResponse.setTicketId(smsResponse.getTicketId());
          botSmsResponse.setRetryCount(smsResponse.getRetryCount() - 1);
          botSmsResponse.setIsAgentMessage(true);
          smsTwilioResponseHandler.postBotResponse(botSmsResponse);
        }
      } else if (!ObjectUtils.isEmpty(smsRetryResponse) && StringUtils.hasText(status)
          && status.equals("delivered")) {
        // delete the cached response if message is delivered
        redisTemplate.delete(String.format(SMS_RETRY_KEY_FORMATTER, messageId));
      }
    } catch (Exception e) {
      log.error("Unable to process twilio status callback for message id %s due to %s", messageId,
          e);
    }
  }

  /**
   * Calls the Ripplese service to fetch bot response for Smsbot API
   * 
   * @param applicationId The application/project ID
   * @param smsUserRequest The SMS user request containing from, to, name, and text
   * @return String containing the bot response message from assistant_response field
   * @throws Exception if the service call fails
   */
  public String fetchBotResponseFromRipples(String applicationId, SmsUserRequest smsUserRequest)
      throws Exception {
    String requestUri = String.format(RIPPLES_SMSBOT_URI_FORMAT,
        serviceDetails.getRipples().getUri(), applicationId);

    Properties headerProperties = new Properties();
    headerProperties.put("X-APIKEY", serviceDetails.getRipples().getAppKey());

    try {
      log.debug("Calling Ripplese service for SMS bot response: {}", requestUri);
      String requestBody = objectMapper.writeValueAsString(smsUserRequest);
      String response =
          restUtils.postRequest(requestUri, requestBody, headerProperties, String.class);

      if (StringUtils.hasText(response)) {
        // Parse the JSON response to extract assistant_response
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("assistant_response").asText();
      } else {
        log.warn("Empty response received from Ripplese service for applicationId: {}",
            applicationId);
        return null;
      }
    } catch (Exception e) {
      log.error("Unable to fetch bot response from Ripplese service for applicationId: {}",
          applicationId, e);
      throw e;
    }
  }

}
