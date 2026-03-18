package ai.concerto.event.service;

import ai.concerto.event.utils.Constants;
import java.util.Collections;

public class Resources {
  public String responseString() {
    return String.format("{\"%s\":" + false + ",\"%s\":\"session\",\"%s\":" + true
        + ",\"%s\":[],\"%s\":\"botReplies\",\"%s\":" + 4.5 + ",\"%s\":\"ctxString\",\"%s\":" + true
        + ",\"%s\":\"hitQAPair\",\"%s\":" + Collections.emptyList()
        + ",\"%s\": \"someSample\",\"%s\": \"timestamp\",\"%s\": \"turnTimestamp\",\"%s\": \"turnNo\",\"%s\": "
        + true + " ,\"%s\": " + false + ",\"%s\": " + false + ",\"%s\": " + true
        + ", \"%s\": \"someLink\",\"%s\": \"passToLiveAgent\", \"%s\": {\"qaResults\":{\"someKey\":\"someValue\"}}"
        + ",\"%s\": \"botRepliesForMail\", \"%s\": \"botRepliesMail\", \"%s\": " + false
        + ",\"%s\": " + Collections.emptyList() + ",\"%s\": \"botRepliesUtilityPhrase\""
        + ",\"%s\" :[\"someText\"], \"%s\": {\"text\":[\"someText\"],\"voice\":[\"someVoice\"],\"email\":[\"someEmail\"],\"image\":[{\"someImage\":\"someImageValue\"}],\"video\":[{\"someVideo\":\"someVideoValue\"}],\"search\":[{\"searchKey\":\"searchValue\"}]"
        + ",\"structured_search\":[{\"someKey\":\"someValue\"}],\"recommend\":[{\"someKey\":\"someValue\"}],\"intent_recommend\":[{\"someKey\":\"someValue\"}],\"alexa_card\":[{\"someKey\":\"someValue\"}],\"echo_card\":[{\"someKey\":\"someValue\"}],\"google_card\":[{\"someKey\":\"someValue\"}],\"rich_cards\":[{\"someKey\":\"someValue\"}],\"echo_show\":[{\"someKey\":\"someValue\"}],\"popup_widget\":{\"someKey\":\"someValue\"}}}",
        Constants.DEResponseConstants.DIALOG_EXIT, Constants.DEResponseConstants.SESSION_ID,
        Constants.DEResponseConstants.BOT_HANDLES_FORM, Constants.DEResponseConstants.BOT_REPEATS,
        Constants.DEResponseConstants.BOT_REPLIES_CHANGE_APP,
        Constants.DEResponseConstants.BOT_REPLIES_CONFIDENCE,
        Constants.DEResponseConstants.BOT_REPLIES_CTX_FORM,
        Constants.DEResponseConstants.BOT_REPLIES_DROPPED_FORM,
        Constants.DEResponseConstants.BOT_REPLIES_HIT_QA_PAIR_ID,
        Constants.DEResponseConstants.BOT_REPLIES_HIT_QA_TAG,
        Constants.DEResponseConstants.BOT_REPLIES_HIT_SAMPLE,
        Constants.DEResponseConstants.TIMESTAMP_MS, Constants.DEResponseConstants.TURN_TIMESTAMP_MS,
        Constants.DEResponseConstants.TURN_NO, Constants.DEResponseConstants.BOT_REPLIES_IN_FORM,
        Constants.DEResponseConstants.BOT_REPLIES_IN_QUIZ,
        Constants.DEResponseConstants.BOT_REPLIES_UNANSWERED,
        Constants.DEResponseConstants.BOT_REPLIES_INVALIDATE_LINK,
        Constants.DEResponseConstants.BOT_REPLIES_INVALID_LINK,
        Constants.DEResponseConstants.BOT_REPLIES_PASS_TO_LIVE_AGENT,
        Constants.DEResponseConstants.BOT_REPLIES_QA_RESULTS,
        Constants.DEResponseConstants.BOT_REPLIES_QUESTION_FOR_EMAIL,
        Constants.DEResponseConstants.BOT_REPLIES_EMAIL,
        Constants.DEResponseConstants.BOT_REPLIES_SEND_EMAIL,
        Constants.DEResponseConstants.BOT_REPLIES_SUGGEST_ASR,
        Constants.DEResponseConstants.BOT_REPLIES_UTILITY_PHRASE,
        Constants.DEResponseConstants.BOT_REPLIES,
        Constants.DEResponseConstants.BOT_REPLIES_OBJECT);
  }
}
