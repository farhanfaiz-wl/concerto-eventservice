package ai.concerto.event.handler.response;

import ai.concerto.event.exchange.BotWhatsappResponse;
import ai.concerto.event.exchange.MessageInfo;
import ai.concerto.event.handler.BotResponseHandler;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WhatsappKaleyraResponseHandler implements BotResponseHandler {

  private static final String KALEYRA_URI_FORMAT = "https://api.kaleyra.io/v1/%s/messages";

  @Autowired
  private ObjectMapper snakeCaseMapper;

  @Autowired
  private RestUtils restUtils;

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Override
  public Object postBotResponse(Object response) {
    BotWhatsappResponse whatsappResponse = (BotWhatsappResponse) response;
    String uri = String.format(KALEYRA_URI_FORMAT, whatsappResponse.getAuthId());
    Properties headerProperties = new Properties();
    headerProperties.put("api-key", whatsappResponse.getAuthToken());

    try {
      String kaleyraRequest = snakeCaseMapper.writeValueAsString(whatsappResponse);
      restUtils.postRequest(uri, kaleyraRequest, MediaType.APPLICATION_FORM_URLENCODED,
          headerProperties, String.class);
      addResponseTime();
      log.info("SuccessFully posted the message to whatsapp kaleyra");
    } catch (Exception e) {
      log.error("Unable to send Kaleyra whatsapp message", e);
      analyticsStreamPublisher.publishErrorLogssAnalytics(whatsappResponse, e.getMessage());
    }
    return response;
  }

  @Override
  public MessageInfo getMessageInfo(Object response) {
    // TODO Auto-generated method stub
    return new MessageInfo();
  }
}
