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
public class SlackRequest {

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class SlackEventBlock {
    private String type;
    private String blockId;
    private List<Map<String, Object>> elements;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class SlackEvent {
    private String botId;
    private String clientMsgId;
    private String type;
    private String subType;
    private String channel;
    private String channelType;
    private String user;
    private String text;
    private String team;
    private List<SlackEventBlock> blocks;
    private String ts;
    private String eventTs;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class SlackAuthorization {
    private String enterpriseId;
    private String teamId;
    private String userId;
    private Boolean isBot;
    private Boolean isEnterpriseInstall;
  }

  private String token;
  private String type;
  private String challenge;
  private String teamId;
  private String apiAppId;
  private String eventId;
  private String eventContext;
  private SlackEvent event;
  private Long eventTime;
  private List<String> authedUsers;
  private List<SlackAuthorization> authorizations;
  private Boolean isExtSharedChannel;
}
