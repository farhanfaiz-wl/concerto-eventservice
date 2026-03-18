package ai.concerto.event.exchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageInfo {

  private String id;
  private String status;
  private String fromEmail;
  private String toEmail;
  private String fromNo;
  private String replyTo;
  private String sentOn;
  private String deliveredOn;
  private String failedDueTo;

}
