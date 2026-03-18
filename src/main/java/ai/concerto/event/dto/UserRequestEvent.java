package ai.concerto.event.dto;

import ai.concerto.event.enums.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestEvent {

  private String applicationId;
  private String request;
  private Channel channel;
  private String vendor;
  private String metaData;
}
