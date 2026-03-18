package ai.concerto.event.exchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacebookRequest {

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FacebookUser {
    private String id;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FacebookQuickReply {
    private String payload;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FacebookAttachment {
    private String type;
    private Map<String, Object> payload;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FacebookMessage {
    private String mid;
    private String text;
    private FacebookQuickReply quickReply;
    private List<FacebookAttachment> attachments;
    private Map<String, Object> replyTo;
    private Map<String, Object> nlp;
    private Map<String, Object> referral;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FacebookPostback {
    private String mid;
    private String title;
    private String payload;
    private Map<String, Object> referral;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FacebookMessaging {
    private FacebookUser sender;
    private FacebookUser recipient;
    private FacebookMessage message;
    private FacebookPostback postback;
    private Long timestamp;
  }

  private String id;
  private Long time;
  private List<FacebookMessaging> messaging;
  private List<FacebookMessaging> standby;
}
