package ai.concerto.event.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AccountLinkingResponse {

  @JsonProperty("accountLinkingResponse")
  private AmazonAccountLinkingResponseParams params;
}
