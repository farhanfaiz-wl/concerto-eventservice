package ai.concerto.event.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AgentMessage {


  private String projectId;
  private String channel;
  private String universalUserId;
  private Message message;
  private Ticket ticket;



  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)

  public static class Message {

    private String text;
    private String actor;
    private String type;
    private LocalDateTime timeStamp;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class Ticket {

    private String id;
    private String number;
  }
}
