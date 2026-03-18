package ai.concerto.event.exchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleBusinessMessageRequest {
  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class GoogleBusinessUserInfo {
    @JsonProperty("displayName")
    private String displayName;
    @JsonProperty("userDeviceLocale")
    private String userDeviceLocale;
  }
  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class GoogleBusinessContext {
    @JsonProperty("placeId")
    private String placeId;
    @JsonProperty("resolvedLocale")
    private String resolvedLocale;
    @JsonProperty("userInfo")
    private GoogleBusinessUserInfo userInfo;
  }
  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class GoogleBusinessMessage {
    private String name;
    private String text;
    @JsonProperty("createTime")
    private String createTime;
    @JsonProperty("messageId")
    private String messageId;
  }
  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class GoogleBusinessSuggestionMessage {
    private String text;
    @JsonProperty("postbackData")
    private String postbackData;
    @JsonProperty("createTime")
    private String createTime;
    private String message;
  }

  @JsonProperty("clientToken")
  private String clientToken;
  private String secret;
  @JsonProperty("suggestionResponse")
  private GoogleBusinessSuggestionMessage suggestionResponse;
  private GoogleBusinessMessage message;
  private GoogleBusinessContext context;
  @JsonProperty("sendTime")
  private String sendTime;
  @JsonProperty("conversationId")
  private String conversationId;
  @JsonProperty("requestId")
  private String requestId;
  private String agent;
}

