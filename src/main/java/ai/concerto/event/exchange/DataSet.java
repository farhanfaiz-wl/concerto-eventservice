package ai.concerto.event.exchange;

import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import ai.concerto.event.enums.DataSetSchema;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSet {
  private String id;
  private DataSetSchema schema;
  private Set<String> tags;
  private Set<String> triggerTags;
  private Map<String, Object> data;
  private Long createdAt;
  private Long updatedAt;
}
