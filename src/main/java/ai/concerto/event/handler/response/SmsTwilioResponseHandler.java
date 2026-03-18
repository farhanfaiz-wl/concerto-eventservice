package ai.concerto.event.handler.response;

import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.exchange.BotSmsResponse;
import ai.concerto.event.exchange.MessageInfo;
import ai.concerto.event.handler.BotResponseHandler;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class SmsTwilioResponseHandler implements BotResponseHandler {

  private static final String SMS_RETRY_KEY_FORMATTER = "evs::sms_retry:%s";

  private static final String TWILIO_STATUS_CALLBACK_URI = "%s/event/sms/twilio/status/%s";

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Autowired
  private ServiceDetails serviceDetails;


  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private RedisTemplate redisTemplate;

  @SuppressWarnings("unchecked")
  @Override
  public Object postBotResponse(Object response) {

    Message message = null;
    if (!ObjectUtils.isEmpty(response)) {
      BotSmsResponse smsResponse = (BotSmsResponse) response;
      try {
        Twilio.init(smsResponse.getAuthId(), smsResponse.getAuthToken());
        message = com.twilio.rest.api.v2010.account.Message
            .creator(new PhoneNumber(smsResponse.getTo()), new PhoneNumber(smsResponse.getFrom()),
                smsResponse.getMessage())
            .setStatusCallback(URI.create(String.format(TWILIO_STATUS_CALLBACK_URI,
                serviceDetails.getEventservice().getUri(), smsResponse.getProjectId())))
            .create();

        if (Boolean.TRUE.equals(smsResponse.getIsAgentMessage())) {
          // cache the smsResponse to be used in case of failure
          redisTemplate.opsForValue().set(String.format(SMS_RETRY_KEY_FORMATTER, message.getSid()),
              objectMapper.writeValueAsString(smsResponse), 30000, TimeUnit.SECONDS);
        }

        addResponseTime();
        log.info("SuccessFully posted the message to sms twilio");
        return message;
      } catch (ApiException e) {
        log.error("Unable to send sms message ,{}", e);
        String error = String.format("%s , More details: %s", e.getMessage(), e.getMoreInfo());
        analyticsStreamPublisher.publishErrorLogssAnalytics(smsResponse, error);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
            "Failed to send sms mesage", e);
      } catch (Exception e) {
        log.error("Unable to send sms message ,{}", e);
        analyticsStreamPublisher.publishErrorLogssAnalytics(smsResponse, e.getMessage());
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
            "Failed to send sms mesage", e);
      }
    }
    return message;
  }

  @Override
  public MessageInfo getMessageInfo(Object response) {
    Message message = (Message) response;
    MessageInfo messageInfo = new MessageInfo();
    messageInfo.setId(message.getSid());
    messageInfo.setFromNo(message.getFrom().getEndpoint());
    messageInfo.setStatus(message.getStatus().name());
    messageInfo.setReplyTo(message.getTo());
    messageInfo.setFailedDueTo(message.getErrorMessage());
    return messageInfo;
  }
}
