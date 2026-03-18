package ai.concerto.event.exchange;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsappCloudApiRequest {

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ButtonReply {
    private String title;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ListReply {
    private String description;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WhatsappText {
    private String body;
    private String type;
    private ButtonReply buttonReply;
    private ListReply listReply;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WhatsappMessage {
    private String from;
    private String id;
    private Long timestamp;
    private String type;
    private WhatsappText text;
    private WhatsappText interactive;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WhatsaappContacts {
    private WhatsaappProfile profile;
    private String waId;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WhatsaappProfile {
    private String name;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WhatsappMetadata {
    private String displayPhoneNumber;
    private String phoneNumberId;
  }

  private WhatsappMetadata metadata;
  private List<WhatsaappContacts> contacts;
  private List<WhatsappMessage> messages;
}
