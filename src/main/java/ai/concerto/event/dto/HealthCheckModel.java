package ai.concerto.event.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;


@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HealthCheckModel {
  @Data
  public static class HealthService {
    public String checker;
    public String output;
    public Boolean passed;
    public long timestamp;
  }

  @Data
  public static class Health {
    public List<HealthService> results;
    public String status;
    public long timestamp;
  }
}
