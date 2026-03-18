package ai.concerto.event.notification.handler;

import java.util.List;
import java.util.Map;
import org.springframework.util.ObjectUtils;
import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.utils.Constants.DEResponseConstants;
import lombok.Data;

@Data
@SuppressWarnings("unchecked")
public class DEBotResponseBuilder {
  private DEBotResponse deBotResponse;
  private Map<String, Object> deResponseMap;

  public static DEBotResponseBuilder builder() {
    return new DEBotResponseBuilder();
  }

  public static DEBotResponseBuilder builder(Map<String, Object> deResponseMap) {
    return new DEBotResponseBuilder().setDeBotResponse(new DEBotResponse())
        .setDeResponseMap(deResponseMap);
  }

  public DEBotResponseBuilder setIsDialogExit() {
    this.deBotResponse
        .setIsDialogExit((Boolean) deResponseMap.get(DEResponseConstants.DIALOG_EXIT));
    return this;
  }

  public DEBotResponseBuilder setSessionId() {
    this.deBotResponse.setSessionId((String) deResponseMap.get(DEResponseConstants.SESSION_ID));
    return this;
  }

  public DEBotResponseBuilder setBotHandlerForm() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_HANDLES_FORM))
      this.deBotResponse
          .setBotHandlesForm((Boolean) deResponseMap.get(DEResponseConstants.BOT_HANDLES_FORM));
    return this;
  }

  public DEBotResponseBuilder setBotRepeats() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPEATS))
      this.deBotResponse
          .setBotRepeats((List<String>) deResponseMap.get(DEResponseConstants.BOT_REPEATS));
    return this;
  }

  public DEBotResponseBuilder setChangeApp() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_CHANGE_APP))
      this.deBotResponse
          .setChangeApp((String) deResponseMap.get(DEResponseConstants.BOT_REPLIES_CHANGE_APP));
    return this;
  }

  public DEBotResponseBuilder setConfidence() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_CONFIDENCE)
        && !ObjectUtils.isEmpty(deResponseMap.get(DEResponseConstants.BOT_REPLIES_CONFIDENCE)))
      this.deBotResponse
          .setConfidence((Double) deResponseMap.get(DEResponseConstants.BOT_REPLIES_CONFIDENCE));
    return this;
  }

  public DEBotResponseBuilder setCtxForm() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_CTX_FORM))
      this.deBotResponse
          .setCtxFrom((String) deResponseMap.get(DEResponseConstants.BOT_REPLIES_CTX_FORM));
    return this;
  }

  public DEBotResponseBuilder setDroppedForm() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_DROPPED_FORM))
      this.deBotResponse.setDroppedForm(
          (Boolean) deResponseMap.get(DEResponseConstants.BOT_REPLIES_DROPPED_FORM));
    return this;
  }

  public DEBotResponseBuilder setHitQAPairId() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_HIT_QA_PAIR_ID))
      this.deBotResponse.setHitQAPairId(
          (String) deResponseMap.get(DEResponseConstants.BOT_REPLIES_HIT_QA_PAIR_ID));
    return this;
  }

  public DEBotResponseBuilder setHitQATag() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_HIT_QA_TAG))
      this.deBotResponse.setHitQATag(
          (List<String>) deResponseMap.get(DEResponseConstants.BOT_REPLIES_HIT_QA_TAG));
    return this;
  }

  public DEBotResponseBuilder setHitSample() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_HIT_SAMPLE))
      this.deBotResponse
          .setHitSample((String) deResponseMap.get(DEResponseConstants.BOT_REPLIES_HIT_SAMPLE));
    return this;
  }

  public DEBotResponseBuilder setTimestampMs() {
    if (deResponseMap.containsKey(DEResponseConstants.TIMESTAMP_MS))
      this.deBotResponse
          .setTimestampMs((String) deResponseMap.get(DEResponseConstants.TIMESTAMP_MS));
    return this;
  }

  public DEBotResponseBuilder setTurnTimestampMs() {
    if (deResponseMap.containsKey(DEResponseConstants.TURN_TIMESTAMP_MS))
      this.deBotResponse
          .setTurnTimestampMs((String) deResponseMap.get(DEResponseConstants.TURN_TIMESTAMP_MS));
    return this;
  }

  public DEBotResponseBuilder setTurnNo() {
    if (deResponseMap.containsKey(DEResponseConstants.TURN_NO))
      this.deBotResponse.setTurnNo((String) deResponseMap.get(DEResponseConstants.TURN_NO));
    return this;
  }

  public DEBotResponseBuilder setInForm() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_IN_FORM))
      this.deBotResponse
          .setInForm((Boolean) deResponseMap.get(DEResponseConstants.BOT_REPLIES_IN_FORM));
    return this;
  }

  public DEBotResponseBuilder setInQuiz() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_IN_QUIZ))
      this.deBotResponse
          .setInQuiz((Boolean) deResponseMap.get(DEResponseConstants.BOT_REPLIES_IN_QUIZ));
    return this;
  }

  public DEBotResponseBuilder setUnanswered() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_UNANSWERED))
      this.deBotResponse
          .setUnanswered((Boolean) deResponseMap.get(DEResponseConstants.BOT_REPLIES_UNANSWERED));
    return this;
  }

  public DEBotResponseBuilder setInvalidateLink() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_INVALIDATE_LINK))
      this.deBotResponse.setInvalidateLink(
          (Boolean) deResponseMap.get(DEResponseConstants.BOT_REPLIES_INVALIDATE_LINK));
    return this;
  }

  public DEBotResponseBuilder setPassToLiveAgent() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_PASS_TO_LIVE_AGENT))
      this.deBotResponse.setPassToLiveAgent(
          (String) deResponseMap.get(DEResponseConstants.BOT_REPLIES_PASS_TO_LIVE_AGENT));
    return this;
  }

  public DEBotResponseBuilder setQaResults() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_QA_RESULTS))
      this.deBotResponse.setQaResults(
          (Map<String, Object>) deResponseMap.get(DEResponseConstants.BOT_REPLIES_QA_RESULTS));
    return this;
  }

  public DEBotResponseBuilder setQuestionForEmail() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_QUESTION_FOR_EMAIL))
      this.deBotResponse.setQuestionForEmail(
          (String) deResponseMap.get(DEResponseConstants.BOT_REPLIES_QUESTION_FOR_EMAIL));
    return this;
  }

  public DEBotResponseBuilder setMailReply() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_EMAIL))
      this.deBotResponse
          .setMailReply((String) deResponseMap.get(DEResponseConstants.BOT_REPLIES_EMAIL));
    return this;
  }

  public DEBotResponseBuilder setSendEmail() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_SEND_EMAIL))
      this.deBotResponse
          .setSendEmail((Boolean) deResponseMap.get(DEResponseConstants.BOT_REPLIES_SEND_EMAIL));
    return this;
  }

  public DEBotResponseBuilder setSuggestASR() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_SUGGEST_ASR))
      this.deBotResponse.setSuggestASR(
          (List<String>) deResponseMap.get(DEResponseConstants.BOT_REPLIES_SUGGEST_ASR));
    return this;
  }

  public DEBotResponseBuilder setUtilityPhrase() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES_UTILITY_PHRASE))
      this.deBotResponse.setUtilityPhrase(
          (String) deResponseMap.get(DEResponseConstants.BOT_REPLIES_UTILITY_PHRASE));
    return this;
  }

  public DEBotResponseBuilder setBotReply() {
    if (deResponseMap.containsKey(DEResponseConstants.BOT_REPLIES))
      this.deBotResponse.getBotReply()
          .setFormatted((List<String>) deResponseMap.get(DEResponseConstants.BOT_REPLIES));

    Map<String, Object> replyMap =
        (Map<String, Object>) deResponseMap.get(DEResponseConstants.BOT_REPLIES_OBJECT);
    this.deBotResponse.getBotReply().setText((List<String>) replyMap.get(DEResponseConstants.TEXT));
    this.deBotResponse.getBotReply()
        .setVoice((List<String>) replyMap.get(DEResponseConstants.VOICE));
    this.deBotResponse.getBotReply()
        .setEmail((List<String>) replyMap.get(DEResponseConstants.EMAIL));
    this.deBotResponse.getBotReply()
        .setImage((List<Map<String, String>>) replyMap.get(DEResponseConstants.IMAGE));
    this.deBotResponse.getBotReply()
        .setVideo((List<Map<String, String>>) replyMap.get(DEResponseConstants.VIDEO));
    this.deBotResponse.getBotReply()
        .setSearch((List<Map<String, Object>>) replyMap.get(DEResponseConstants.SEARCH));
    this.deBotResponse.getBotReply().setStructuredSearch(
        (List<Map<String, Object>>) replyMap.get(DEResponseConstants.STRUCTURED_SEARCH));
    this.deBotResponse.getBotReply()
        .setRecommend((List<Map<String, String>>) replyMap.get(DEResponseConstants.RECOMMEND));
    this.deBotResponse.getBotReply().setIntentRecommend(
        (List<Map<String, String>>) replyMap.get(DEResponseConstants.INTENT_RECOMMEND));
    this.deBotResponse.getBotReply()
        .setAlexaCard((List<Map<String, Object>>) replyMap.get(DEResponseConstants.ALEXA_CARD));
    this.deBotResponse.getBotReply()
        .setEchoCard((List<Map<String, Object>>) replyMap.get(DEResponseConstants.ECHO_CARD));
    this.deBotResponse.getBotReply()
        .setGoogleCard((List<Map<String, Object>>) replyMap.get(DEResponseConstants.GOOGLE_CARD));
    this.deBotResponse.getBotReply()
        .setRichCards((List<Map<String, Object>>) replyMap.get(DEResponseConstants.RICH_CARDS));
    this.deBotResponse.getBotReply()
        .setEchoShow((List<Map<String, Object>>) replyMap.get(DEResponseConstants.ECHO_SHOW));
    this.deBotResponse.getBotReply().setPopupWidget(
        (Map<String, Object>) replyMap.get(DEResponseConstants.BOT_REPLIES_POPUP_WIDGET));
    this.deBotResponse.getBotReply()
        .setRichInput((Map<String, Object>) replyMap.get(DEResponseConstants.RICH_INPUT));
    this.deBotResponse
        .setIntentRecommend(!this.deBotResponse.getBotReply().getIntentRecommend().isEmpty());

    return this;
  }

  public DEBotResponseBuilder setDeBotResponse(DEBotResponse response) {
    this.deBotResponse = response;
    return this;
  }

  public DEBotResponseBuilder setDeResponseMap(Map<String, Object> deResponseMap) {
    this.deResponseMap = deResponseMap;
    return this;
  }

  public DEBotResponse build() {
    return this.deBotResponse;
  }
}
