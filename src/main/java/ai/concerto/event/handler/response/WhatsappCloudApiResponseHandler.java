package ai.concerto.event.handler.response;

import ai.concerto.event.exchange.BotWhatsappCloudApiResponse;
import ai.concerto.event.exchange.MessageInfo;
import ai.concerto.event.handler.BotResponseHandler;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WhatsappCloudApiResponseHandler implements BotResponseHandler {

  private static final String CLOUD_API_MESSAGE_URI =
      "https://graph.facebook.com/v13.0/%s/messages";

  @Autowired
  private ObjectMapper snakeCaseMapper;

  @Autowired
  private RestUtils restUtils;

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Override
  public Object postBotResponse(Object response) {
    BotWhatsappCloudApiResponse whatsappCloudApiResponse = (BotWhatsappCloudApiResponse) response;
    Properties headerProperties = new Properties();

    try {
      headerProperties.put("Authorization", whatsappCloudApiResponse.getAccessToken());
      String messageRequest = snakeCaseMapper.writeValueAsString(whatsappCloudApiResponse);
      restUtils.postRequest(
          String.format(CLOUD_API_MESSAGE_URI, whatsappCloudApiResponse.getPhoneNumberId()),
          messageRequest, headerProperties, String.class);
      addResponseTime();
      log.info("SuccessFully posted the message to whatsapp cloud api");
    } catch (Exception e) {
      log.error("Unable to send cloud api whatsapp message", e);
      analyticsStreamPublisher.publishErrorLogssAnalytics(whatsappCloudApiResponse, e.getMessage());
    }
    return response;
  }

  @Override
  public MessageInfo getMessageInfo(Object response) {
    // TODO Auto-generated method stub
    return new MessageInfo();
  }
}
