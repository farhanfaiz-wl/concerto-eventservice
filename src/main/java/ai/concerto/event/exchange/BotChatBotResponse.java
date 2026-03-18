package ai.concerto.event.exchange;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.HashMap;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotChatBotResponse extends BotResponse {

  private List<String> botReplies;
  private List<String> botRepeats;
  private List<HashMap<String, String>> botRepliesRcmd;
  private List<String> botRepliesText;
  private String botRepliesVoice;
  private HashMap<String, Object> botRepliesCard;
  private HashMap<String, Object> botRepliesGoogleCard;
  private List<HashMap<String, Object>> botRepliesImage;
  private List<HashMap<String, String>> botRepliesVideo;
  private List<String> botRepliesSuggestASR;
  private List<HashMap<String, Object>> botRepliesSearch;
  private List<HashMap<String, Object>> botRepliesStructuredSearch;
  private HashMap<String, Object> botRepliesRichInput;
  private HashMap<String, Object> botRepliesQAResults;
  private HashMap<String, Object> botRepliesQuestionForEmail;
  private String botRepliesEmail;
  private float botRepliesConfidence;
  private String botRepliesHitSample;
  private String botRepliesUtilityPhrase;
  private String botRepliesHitQAPairId;
  private List<String> botRepliesHitQATag;
  private List<HashMap<String, Object>> botRepliesRichCard;
  private String botRepliesChangeApp;
  private String botRepliesPassToLiveAgent;
  private String botRepliesCtxFrom;
  private Boolean botRepliesSendEmail;
  private Boolean botRepliesInvalidateLink;
  private Boolean botRepliesInQuiz;
  private boolean botRepliesDroppedForm;
  private boolean botRepliesUnanswered;
  private boolean botRepliesInForm;
  private boolean botHandlesForm;
  private boolean intentRcmd;
  private String timestampMs;
  private String turnTimestampMs;
  private String turnNo;
  private HashMap<String, String> botVoice;
  private HashMap<String, Object> botReplyPopUpWidget;
  @JsonIgnore
  private String webSocketUserName;
}
