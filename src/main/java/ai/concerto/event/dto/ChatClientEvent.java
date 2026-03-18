package ai.concerto.event.dto;

import ai.concerto.event.exchange.ChatClientEventRequest;
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
public class ChatClientEvent {

  private String projectId;
  private String sessionId;
  private String userId;
  private String userIpAddress;
  private String userAgent;
  private String eventType;
  private String eventLink;
  private Long timestampS;

  public static ChatClientEvent from(ChatClientEventRequest eventRequest) {
    ChatClientEvent event = new ChatClientEvent();
    event.setProjectId(eventRequest.getProjectId());
    event.setUserId(eventRequest.getUser().getUserId());
    event.setUserIpAddress(eventRequest.getUser().getIpAddress());
    event.setUserAgent(eventRequest.getUser().getAgent());
    event.setEventType(eventRequest.getEventType());
    event.setEventLink(eventRequest.getEventLink());

    return event;
  }
}
