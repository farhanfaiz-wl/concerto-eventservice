package ai.concerto.event.exchange;

import ai.concerto.event.exchange.StatusResponse.Status;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProjectCloneResponse {

  @Data
  @JsonInclude(Include.NON_NULL)
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Application {
    @JsonProperty("_id")
    private String id;
    private String name;
  }

  private List<Application> applications;
  private Status status;
}
