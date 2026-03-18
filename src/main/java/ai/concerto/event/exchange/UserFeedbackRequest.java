package ai.concerto.event.exchange;

import java.util.Base64;
import java.util.Optional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UserFeedbackRequest {
  private Boolean answerHelpful;
  private String channel;
  private String projectId;
  private String qaPairId;
  private String userId;
  private String turnId;

  @JsonIgnore
  public String encode() {
    final StringBuilder feedBack = new StringBuilder();
    if (!ObjectUtils.isEmpty(answerHelpful)) {
      feedBack.append(answerHelpful);
    }
    feedBack.append("|");
    feedBack.append(Optional.ofNullable(channel).orElse(""));
    feedBack.append("|");
    feedBack.append(Optional.ofNullable(projectId).orElse(""));
    feedBack.append("|");
    feedBack.append(Optional.ofNullable(qaPairId).orElse(""));
    feedBack.append("|");
    feedBack.append(Optional.ofNullable(userId).orElse(""));
    feedBack.append("|");
    feedBack.append(Optional.ofNullable(turnId).orElse(""));
    return Base64.getEncoder().encodeToString(feedBack.toString().getBytes());
  }

  public static UserFeedbackRequest decode(String feedback) {
    try {
      String[] decodedState = new String(Base64.getDecoder().decode(feedback)).split("\\|");
      UserFeedbackRequest request = new UserFeedbackRequest();
      if (StringUtils.hasText(decodedState[0])) {
        request.setAnswerHelpful(Boolean.valueOf(decodedState[0]));
      }
      if (StringUtils.hasText(decodedState[1])) {
        request.setChannel(decodedState[1]);
      }
      if (StringUtils.hasText(decodedState[2])) {
        request.setProjectId(decodedState[2]);
      }
      if (StringUtils.hasText(decodedState[3])) {
        request.setQaPairId(decodedState[3]);
      }
      if (StringUtils.hasText(decodedState[4])) {
        request.setUserId(decodedState[4]);
      }
      if (StringUtils.hasText(decodedState[5])) {
        request.setTurnId(decodedState[5]);
      }
      return request;
    } catch (Exception e) {
      log.error("Error in parsing UserFeedbackRequest : {}", feedback, e);
    }
    return null;
  }
}
