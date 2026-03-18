package ai.concerto.event.exchange;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AmazonAccountLinkingResponseParams {

  @JsonProperty("skipOnEnablement")
  private Boolean skipOnEnablement;
  @JsonProperty("type")
  private String type;
  @JsonProperty("authorizationUrl")
  private String authorizationUrl;
  @JsonProperty("clientId")
  private String clientId;
  @JsonProperty("accessTokenUrl")
  private String accessTokenUrl;
  @JsonProperty("accessTokenScheme")
  private String accessTokenScheme;
  @JsonProperty("defaultTokenExpirationInSeconds")
  private Integer defaultTokenExpirationInSeconds;
  @JsonProperty("scopes")
  private List<String> scopes;
  @JsonProperty("domains")
  private List<String> domains;
  @JsonProperty("redirectUrls")
  private List<String> redirectUrls;
}
