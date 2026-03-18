package ai.concerto.event.exchange;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotSmsResponse extends BotResponse {

  @JsonIgnore
  private String authId;
  @JsonIgnore
  private String authToken;

  private String from;
  private String to;
  private String message;
  private String ticketId;
  private Integer retryCount = 3;
  private Boolean isAgentMessage = false;
}
