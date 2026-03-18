package ai.concerto.event.exchange;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotWhatsappCloudApiResponse extends BotResponse {

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WhatsappText {
    private String body;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WhatsappBody {
    private String text;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WhatsappReply {
    private String id;
    private String title;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Button {
    private String type;
    private WhatsappReply reply = new WhatsappReply();
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Row {
    private String id;
    private String title;
    private String description;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Section {
    private String title;
    private List<Row> rows;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WhatsappAction {
    private List<Button> buttons;
    private String button;
    private List<Section> sections;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WhatsappInteractive {
    private String type;
    private WhatsappBody body = new WhatsappBody();
    private WhatsappAction action = new WhatsappAction();
  }

  private String messagingProduct;
  private String to;
  private String type;
  private WhatsappText text;
  private WhatsappInteractive interactive;

  @JsonIgnore
  private String accessToken;
  @JsonIgnore
  private String phoneNumberId;

}
