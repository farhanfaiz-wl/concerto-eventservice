package ai.concerto.event.exchange;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatBotSearchResponse {

  @JsonProperty("botRepliesQAResults")
  private List<DataSet> botRepliesQAResults;
  @JsonProperty("turnId")
  private String turnId;
  @JsonProperty("botRepliesSearch")
  private List<Map<String, Object>> botRepliesSearch;
  @JsonProperty("botRepliesStructuredSearch")
  private List<Map<String, Object>> botRepliesStructuredSearch;
  @JsonProperty("botRepliesRcmd")
  private List<Map<String, String>> botRepliesRcmd;

}
