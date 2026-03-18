package ai.concerto.event.exchange;

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
public class BotWhatsappRouteResponse extends BotResponse {

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WhatsappRouteMedia {
    private String type;
    private String url;
    private String caption;
  }

  @JsonIgnore
  private String authId;
  @JsonIgnore
  private String authToken;

  private String phone;
  private String text;
  private WhatsappRouteMedia media;
}
