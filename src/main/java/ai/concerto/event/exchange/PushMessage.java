package ai.concerto.event.exchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Set;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PushMessage {

  private String userId;
  private String universalUserId;
  private String turnId;
  private String sessionId;
  private String projectId;
  private String ticketId;
  private String to;
  private Set<String> emailCc;
  private Set<String> emailBcc;
  private Set<String> toEmails;
  private String from;
  private String channel;
  private String vendor;
  private String message;
  private String subject;
  private String slackChannel;
  private Boolean isNotification;

}
