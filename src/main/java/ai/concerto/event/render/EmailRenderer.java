package ai.concerto.event.render;

import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Source;
import ai.concerto.event.exchange.EmailResponse;
import ai.concerto.event.exchange.EmailResponse.EmailMessage;
import ai.concerto.event.exchange.UserFeedbackRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Slf4j
@Service
public class EmailRenderer implements ResponseRenderer {

  private static final String FEEDBACK_URL = "https://%s/event/feedback?payload=%s";

  @SuppressWarnings("unchecked")
  @Override
  public Object render(Object userRequest, DERequest deRequest, DEBotResponse deResponse) {

    Map<String, String> emailRequest = (Map<String, String>) userRequest;

    EmailResponse emailResponse = new EmailResponse();
    emailResponse.setUser(deRequest.getEmail());
    emailResponse.setProjectId(deRequest.getProjectId());
    emailResponse.setIsAsync(Boolean.valueOf(emailRequest.get("isAsync")));

    EmailMessage message = emailResponse.new EmailMessage();
    List<String> botReplies = deResponse.getBotReply().getText();
    message.setBotReplies(botReplies);
    emailResponse.setResponse(message);

    List<Map<String, String>> rcmdList = deResponse.getBotReply().getRecommend();
    List<Map<String, String>> rcmdListEmail = new ArrayList<>();
    if (!ObjectUtils.isEmpty(rcmdList) && !rcmdList.isEmpty()) {
      for (int i = 0; i < 3 && i < rcmdList.size(); i++) {
        rcmdListEmail.add(rcmdList.get(i));
      }
    }
    emailResponse.getResponse().setBotRepliesRcmd(rcmdListEmail);

    Map<String, Object> botRepliesQAResult = deResponse.getQaResults();
    emailResponse.getResponse().setBotRepliesQaResults(botRepliesQAResult);


    if (!ObjectUtils.isEmpty(botReplies) && !botReplies.isEmpty()) {
      try {
        UserFeedbackRequest feedback = new UserFeedbackRequest();
        feedback.setAnswerHelpful(true);
        feedback.setChannel(Source.email.name());
        feedback.setProjectId(deRequest.getProjectId());
        feedback.setUserId(deRequest.getEmail());
        feedback.setTurnId(deRequest.getTurnId());

        emailResponse.getResponse()
            .setPositiveFeedback(String.format(FEEDBACK_URL,
                Optional.ofNullable(deRequest.getHost()).orElse("localhost:9090"),
                URLEncoder.encode(feedback.encode(), Charset.defaultCharset().name())));

        emailResponse.getResponse()
            .setNegativeFeedback(String.format(FEEDBACK_URL,
                Optional.ofNullable(deRequest.getHost()).orElse("localhost:9090"),
                URLEncoder.encode(feedback.encode(), Charset.defaultCharset().name())));
      } catch (UnsupportedEncodingException e) {
        log.error("Error occurred while rendering email client response {}", e);
      }
    }

    emailResponse.getResponse().setUserInputLast(deRequest.getUserInputLast());
    return emailResponse;
  }

}
