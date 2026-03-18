package ai.concerto.event.exchange;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailResponse extends BotResponse {

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public class EmailMessage {
    private List<Map<String, String>> botRepliesRcmd;
    private List<String> botReplies;
    private Map<String, Object> botRepliesQaResults;
    private String positiveFeedback;
    private String negativeFeedback;
    private String userInputLast;
    private String subject;
    private Set<String> emailCc;
    private Set<String> emailBcc;
    private Set<String> toEmails;

  }

  private String user;
  private String projectId;
  private EmailMessage response = new EmailMessage();
  @JsonIgnore
  private Boolean isAsync;
  @JsonIgnore
  private Map<String, Object> responseMessage;

}
