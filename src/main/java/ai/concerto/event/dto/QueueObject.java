package ai.concerto.event.dto;

import ai.concerto.event.enums.Channel;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class QueueObject implements Serializable {

  private static final long serialVersionUID = 1L;
  private Channel channel;
  private List<Object> params;

  public QueueObject(Channel channel, List<Object> params) {
    this.channel = channel;
    this.params = params;
  }
}
