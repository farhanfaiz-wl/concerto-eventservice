package ai.concerto.event.dto;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DEBotResponse {

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class BotReply {
    private List<String> text;
    private List<String> voice;
    private List<String> formatted;
    private List<String> email;
    private List<Map<String, String>> image;
    private List<Map<String, String>> video;
    private List<Map<String, Object>> search;
    private List<Map<String, Object>> structuredSearch;
    private List<Map<String, String>> recommend;
    private List<Map<String, String>> intentRecommend;
    private List<Map<String, Object>> alexaCard;
    private List<Map<String, Object>> echoCard;
    private List<Map<String, Object>> googleCard;
    private List<Map<String, Object>> richCards;
    private List<Map<String, Object>> echoShow;
    private Map<String, Object> popupWidget;
    private Map<String, Object> richInput;
  }



  private String sessionId;
  private double confidence;
  private String hitSample;
  private BotReply botReply = new BotReply();
  private Boolean sendEmail;
  private Boolean isDialogExit;
  private boolean inForm;
  private boolean inQuiz;
  private boolean unanswered;
  private boolean droppedForm;
  private boolean intentRecommend;
  private String invalidLink;
  private String passToLiveAgent;
  private String utilityPhrase;
  private String changeApp;
  private String questionForEmail;
  private String mailReply;
  private Map<String, Object> qaResults;
  private List<String> suggestASR;

  private List<String> botRepeats;
  private String hitQAPairId;
  private List<String> hitQATag;
  private String ctxFrom;
  private boolean botHandlesForm;
  private boolean invalidateLink;
  private String timestampMs;
  private String turnTimestampMs;
  private String turnNo;
}
