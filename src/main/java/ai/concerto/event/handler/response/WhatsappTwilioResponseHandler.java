package ai.concerto.event.handler.response;

import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.exchange.BotWhatsappResponse;
import ai.concerto.event.exchange.MessageInfo;
import ai.concerto.event.handler.BotResponseHandler;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import java.net.URI;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class WhatsappTwilioResponseHandler implements BotResponseHandler {

  private static final String WHATSAPP_PREFIX = "whatsapp:";
  private static final String TWILIO_STATUS_CALLBACK_URI = "%s/event/whatsapp/twilio/status/%s";
  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Autowired
  private ServiceDetails serviceDetails;

  @Override
  public Object postBotResponse(Object response) {

    Message message = null;
    if (!ObjectUtils.isEmpty(response)) {
      BotWhatsappResponse whatsappResponse = (BotWhatsappResponse) response;
      Twilio.init(whatsappResponse.getAuthId(), whatsappResponse.getAuthToken());

      StringBuilder messageBuilder = new StringBuilder();
      messageBuilder.append(whatsappResponse.getBody().replace("\\\\n", "\n"));

      if (StringUtils.hasText(whatsappResponse.getMediaUrl()) && whatsappResponse.isVideo()) {
        messageBuilder.append(System.lineSeparator()).append(whatsappResponse.getMediaUrl());
        whatsappResponse.setMediaUrl(null);
      }

      try {
        MessageCreator messageCreator = com.twilio.rest.api.v2010.account.Message.creator(
            new PhoneNumber(WHATSAPP_PREFIX.concat(whatsappResponse.getTo())),
            new PhoneNumber(WHATSAPP_PREFIX.concat(whatsappResponse.getFrom())),
            messageBuilder.toString());
        if (StringUtils.hasText(whatsappResponse.getMediaUrl())) {
          messageCreator.setMediaUrl(Arrays.asList(URI.create(whatsappResponse.getMediaUrl())));
        }
        message = messageCreator
            .setStatusCallback(URI.create(String.format(TWILIO_STATUS_CALLBACK_URI,
                serviceDetails.getEventservice().getUri(), whatsappResponse.getProjectId())))
            .create();

        addResponseTime();
        log.info("SuccessFully posted the message to whatsapp twilio");
      } catch (Exception e) {
        log.error("Unable to send twilio whatsapp message", e);
        analyticsStreamPublisher.publishErrorLogssAnalytics(whatsappResponse, e.getMessage());
      }
    }
    return message;
  }

  @Override
  public MessageInfo getMessageInfo(Object response) {
    Message message = (Message) response;
    MessageInfo messageInfo = new MessageInfo();
    messageInfo.setId(message.getSid());
    messageInfo.setFromNo(message.getFrom().getEndpoint().split(":")[1]);
    messageInfo.setStatus(message.getStatus().name());
    messageInfo.setReplyTo(message.getTo().split(":")[1]);
    messageInfo.setFailedDueTo(message.getErrorMessage());
    return messageInfo;
  }

}
