package ai.concerto.event.handler.response;

import ai.concerto.event.exchange.BotWhatsappResponse;
import ai.concerto.event.exchange.MessageInfo;
import ai.concerto.event.exchange.VfRequest;
import ai.concerto.event.exchange.VfRequest.VfAddress;
import ai.concerto.event.exchange.VfRequest.VfMessage;
import ai.concerto.event.exchange.VfRequest.VfMessage.VfMessageBuilder;
import ai.concerto.event.exchange.VfRequest.VfUser;
import ai.concerto.event.handler.BotResponseHandler;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class WhatsappVFResponseHandler implements BotResponseHandler {

  private static final String VF_URI_FORMAT =
      "https://api.myvfirst.com/psms/servlet/psms.JsonEservice";

  @Autowired
  private RestUtils restUtils;

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Override
  public Object postBotResponse(Object response) {
    BotWhatsappResponse whatsappResponse = (BotWhatsappResponse) response;
    try {
      restUtils.postRequest(VF_URI_FORMAT, generateVfRequest(whatsappResponse), null, String.class);
      addResponseTime();
      log.info("SuccessFully posted the message to whatsapp VF");
    } catch (Exception ex) {
      log.error("Unable to send whatsapp message with vendor {},{}", "VF", ex);
      analyticsStreamPublisher.publishErrorLogssAnalytics(whatsappResponse, ex.getMessage());
    }
    return response;
  }


  private String generateVfRequest(BotWhatsappResponse whatsappResponse)
      throws JsonProcessingException {

    String text = whatsappResponse.getBody().replace("\\\\n", "\n");
    VfUser user = VfUser.builder().username(whatsappResponse.getAuthId())
        .password(whatsappResponse.getAuthToken()).chType("4").unixTimestamp("").build();
    VfAddress address = VfAddress.builder().from(whatsappResponse.getFrom())
        .to(whatsappResponse.getTo()).seq("1").tag("").build();

    VfMessageBuilder messageBuilder = VfMessage.builder();
    if (StringUtils.hasText(whatsappResponse.getMediaUrl())) {
      log.info("Vf Response has media :" + whatsappResponse.getMediaUrl());
      if (whatsappResponse.isVideo()) {
        messageBuilder = messageBuilder.id("").udh("0").coding("1").msgType("4").property("0")
            .text("").caption(text).type("video").contentType("video/mp4")
            .mediaData(whatsappResponse.getMediaUrl()).addresses(Arrays.asList(address));
      } else {
        messageBuilder = messageBuilder.id("").udh("0").coding("1").msgType("4").property("0")
            .text("").caption(text).type("image").contentType("image/jpg")
            .mediaData(whatsappResponse.getMediaUrl()).addresses(Arrays.asList(address));
      }
    } else {
      messageBuilder = messageBuilder.id("").udh("0").coding("1").msgType("2").property("0")
          .text(text).addresses(Arrays.asList(address));
    }
    VfRequest request = new VfRequest("1.2", user, Arrays.asList(messageBuilder.build()));
    log.info("Vf Response :" + mapper.writeValueAsString(request));
    return mapper.writeValueAsString(request);

  }

  @Override
  public MessageInfo getMessageInfo(Object response) {
    // TODO Auto-generated method stub
    return new MessageInfo();
  }
}
