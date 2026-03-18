package ai.concerto.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Session {

  private String sessionId;
  private boolean isInsideForm;
  private boolean isInsideQuiz;
  private boolean isNew;
  private String slackChannel;
  private String whatsappPhoneNumberId;
  private String userId;
  private Integer silentCount;
  private boolean processUserUtternace;
  private String webSocketUserName;

  public Session(String sessionId) {
    this.sessionId = sessionId;
    this.isNew = true;
    this.isInsideForm = false;
    this.isInsideQuiz = false;
    this.processUserUtternace = false;
  }
}
