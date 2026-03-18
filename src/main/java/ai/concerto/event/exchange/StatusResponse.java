package ai.concerto.event.exchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusResponse {

  private Status status = new Status();

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Status {

    private Integer code;
    private String type;
    private String message;
  }
}
