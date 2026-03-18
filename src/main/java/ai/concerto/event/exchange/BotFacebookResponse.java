package ai.concerto.event.exchange;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotFacebookResponse extends BotResponse {

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FbReplyRecipient {
    private String id;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FbReplyMessage {
    private String text;
    private String metadata;
    private List<FbReplyQuickreplies> quickReplies;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FbReplyQuickreplies {
    private String content_type;
    private String title;
    private String payload;
  }

  private FbReplyRecipient recipient = new FbReplyRecipient();
  private FbReplyMessage message = new FbReplyMessage();

}
