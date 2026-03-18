package ai.concerto.event.handler.response;

import ai.concerto.event.exchange.EmailResponse;
import ai.concerto.event.exchange.MessageInfo;
import ai.concerto.event.handler.BotResponseHandler;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Slf4j
@Service
public class EmailResponseHandler implements BotResponseHandler {

  @Autowired
  private EmailService emailService;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Override
  public Object postBotResponse(Object response) {
    EmailResponse emailResponse = (EmailResponse) response;

    Map<String, Object> responseMessage = null;
    try {
      if (!ObjectUtils.isEmpty(emailResponse.getIsAsync())
          && Boolean.TRUE.equals(emailResponse.getIsAsync()) && !ObjectUtils.isEmpty(response)) {
        responseMessage = emailService.sendEmail(emailResponse.getProjectId(),
            objectMapper.writeValueAsString(response));
        emailResponse.setResponseMessage(responseMessage);
      }
    } catch (Exception e) {
      log.error("Error occurred while processing email request", e);
      analyticsStreamPublisher.publishErrorLogssAnalytics(emailResponse, e.getMessage());
    }
    return emailResponse;
  }

  @Override
  public MessageInfo getMessageInfo(Object response) {
    EmailResponse message = (EmailResponse) response;
    MessageInfo messageInfo = new MessageInfo();
    messageInfo
        .setToEmail(message.getResponse().getToEmails().stream().collect(Collectors.joining(",")));
    messageInfo.setStatus("SENT");
    // update status as failed if we get status != success from email client
    message.getResponseMessage().entrySet().stream().forEach(entry -> {
      if (!entry.getValue().toString().equals("success")) {
        messageInfo.setStatus("FAILED");
        messageInfo.setFailedDueTo(entry.getValue().toString());
      }
    });
    return messageInfo;
  }

}
