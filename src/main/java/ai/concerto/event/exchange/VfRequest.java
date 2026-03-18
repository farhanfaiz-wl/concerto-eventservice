package ai.concerto.event.exchange;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VfRequest {

  @Data
  @Builder
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class VfUser {

    @JsonProperty("@USERNAME")
    private String username;

    @JsonProperty("@PASSWORD")
    private String password;

    @JsonProperty("@CH_TYPE")
    private String chType;

    @JsonProperty("@UNIXTIMESTAMP")
    private String unixTimestamp;

  }

  @Data
  @Builder
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class VfMessage {

    @JsonProperty("@ID")
    private String id;

    @JsonProperty("@UDH")
    private String udh;

    @JsonProperty("@CODING")
    private String coding;

    @JsonProperty("@CONTENTTYPE")
    private String contentType;

    @JsonProperty("@TYPE")
    private String type;

    @JsonProperty("@MEDIADATA")
    private String mediaData;

    @JsonProperty("@MSGTYPE")
    private String msgType;

    @JsonProperty("@PROPERTY")
    private String property;

    @JsonProperty("@CAPTION")
    private String caption;

    @JsonProperty("@TEXT")
    private String text;

    @JsonProperty("ADDRESS")
    private List<VfAddress> addresses;

  }

  @Data
  @Builder
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class VfAddress {

    @JsonProperty("@FROM")
    private String from;

    @JsonProperty("@TO")
    private String to;

    @JsonProperty("@SEG")
    private String seq;

    @JsonProperty("@TAG")
    private String tag;

  }

  @JsonProperty("@VER")
  private String version;

  @JsonProperty("USER")
  private VfUser user;

  @JsonProperty("SMS")
  private List<VfMessage> messages;
}
