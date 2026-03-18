package ai.concerto.event.exchange;

import ai.concerto.event.dto.AgentDetail;
import ai.concerto.event.dto.UserInfo;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.EventType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ControlChangeEvent implements Event {

  private EventType type;
  private UserInfo user;
  private String applicationId;
  private Channel channel;
  private String control;
  private AgentDetail agentDetail;
  private Long timestampS;

  @Override
  public String data(ObjectMapper mapper) {
    try {
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      log.error("Unable to serialize control change event", e);
    }
    return null;
  }
}
