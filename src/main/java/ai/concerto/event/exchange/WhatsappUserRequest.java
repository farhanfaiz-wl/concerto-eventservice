package ai.concerto.event.exchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsappUserRequest {

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WhatsappProfile {
    private String name;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WhatsappContact {
    private String waId;
    private WhatsappProfile profile;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WhatsappMessageText {
    private String body;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WhatsappMessage {
    private String id;
    private String from;
    private WhatsappMessageText text;
    private String timestamp;
    private String type;
  }

  private List<WhatsappContact> contacts;
  private List<WhatsappMessage> messages;

  public WhatsappUserRequest() {
    contacts = new ArrayList<>();
    messages = new ArrayList<>();
  }

  public WhatsappUserRequest(
      String from, String name, String type, String body, String createdAt, String replyTo) {
    this();
    WhatsappProfile profile = new WhatsappProfile();
    profile.setName(name);
    WhatsappContact contact = new WhatsappContact();
    contact.setProfile(profile);
    contact.setWaId(from);
    contacts.add(contact);

    WhatsappMessageText messageText = new WhatsappMessageText();
    messageText.setBody(body);
    WhatsappMessage message = new WhatsappMessage();
    message.setId(replyTo);
    message.setFrom(from);
    message.setText(messageText);
    message.setType(type);
    message.setTimestamp(createdAt);
    messages.add(message);
  }
}
