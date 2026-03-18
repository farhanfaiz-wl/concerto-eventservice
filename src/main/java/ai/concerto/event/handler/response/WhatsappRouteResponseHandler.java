package ai.concerto.event.handler.response;

import ai.concerto.event.exchange.BotWhatsappRouteResponse;
import ai.concerto.event.exchange.MessageInfo;
import ai.concerto.event.handler.BotResponseHandler;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WhatsappRouteResponseHandler implements BotResponseHandler {

  private static final String ROUTE_AUTH_URI = "https://apis.rmlconnect.net/auth/v1/login/";

  private static final String ROUTE_MESSAGE_URI = "https://apis.rmlconnect.net/wba/v1/messages";

  @Autowired
  private ObjectMapper snakeCaseMapper;

  @Autowired
  private RestUtils restUtils;

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Override
  public Object postBotResponse(Object response) {
    BotWhatsappRouteResponse whatsappResponse = (BotWhatsappRouteResponse) response;
    Properties headerProperties = new Properties();

    try {
      headerProperties.put("Authorization",
          getRouteAuthToken(whatsappResponse.getAuthId(), whatsappResponse.getAuthToken()));
      String routeRequest = snakeCaseMapper.writeValueAsString(whatsappResponse);
      restUtils.postRequest(ROUTE_MESSAGE_URI, routeRequest, headerProperties, String.class);
      addResponseTime();
      log.info("SuccessFully posted the message to whatsapp route");
    } catch (Exception e) {
      log.error("Unable to send Route whatsapp message", e);
      analyticsStreamPublisher.publishErrorLogssAnalytics(whatsappResponse, e.getMessage());
    }
    return response;
  }

  private String getRouteAuthToken(String username, String password) throws Exception {
    Map<String, String> authRequestMap = new HashMap<>();
    authRequestMap.put("username", username);
    authRequestMap.put("password", password);
    String authRequest = snakeCaseMapper.writeValueAsString(authRequestMap);
    String authResponse =
        restUtils.postRequest(ROUTE_AUTH_URI, authRequest, new Properties(), String.class);
    Map<String, Object> authResponseMap =
        snakeCaseMapper.readValue(authResponse, new TypeReference<Map<String, Object>>() {});

    return (String) authResponseMap.get("JWTAUTH");
  }

  @Override
  public MessageInfo getMessageInfo(Object response) {
    // TODO Auto-generated method stub
    return new MessageInfo();
  }
}
