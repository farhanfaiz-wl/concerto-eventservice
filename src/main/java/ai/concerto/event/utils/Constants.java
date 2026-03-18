package ai.concerto.event.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.twilio.twiml.voice.Say;
import com.vonage.client.voice.TextToSpeechLanguage;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class HttpConstants {

    public static final String HEADER_KEY_CORRELATION_ID = "X-Correlation-Id";
    public static final String HEADER_KEY_APP_NAME = "X-APP-NAME";
    public static final String HEADER_KEY_APP_TOKEN = "X-APP-TOKEN";
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class MdcConstants {

    public static final String CORRELATION_ID = "correlation_id";
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class DEResponseConstants {

    public static final String SESSION_ID = "session_id";
    public static final String DIALOG_EXIT = "dialogExit";
    public static final String BOT_REPLIES = "botReplies";
    public static final String BOT_REPEATS = "botRepeats";
    public static final String BOT_REPLIES_INTENT_RCMD = "botRepliesIntentRcmd";
    public static final String BOT_REPLIES_RCMD = "botRepliesRcmd";
    public static final String BOT_REPLIES_TEXT = "botRepliesText";
    public static final String BOT_REPLIES_VOICE = "botRepliesVoice";
    public static final String BOT_REPLIES_GOOGLE_CARD = "botRepliesGoogleCard";
    public static final String BOT_REPLIES_IMAGE = "botRepliesImage";
    public static final String BOT_REPLIES_VIDEO = "botRepliesVideo";
    public static final String BOT_REPLIES_SUGGEST_ASR = "botRepliesSuggestASR";
    public static final String BOT_REPLIES_SEARCH = "botRepliesSearch";
    public static final String BOT_REPLIES_STRUCTURED_SEARCH = "botRepliesStructuredSearch";
    public static final String BOT_REPLIES_QA_RESULTS = "botRepliesQAResults";
    public static final String BOT_REPLIES_QUESTION_FOR_EMAIL = "botRepliesQuestionForEmail";
    public static final String BOT_REPLIES_EMAIL = "botRepliesEmail";
    public static final String BOT_REPLIES_CONFIDENCE = "botRepliesConfidence";
    public static final String BOT_REPLIES_HIT_SAMPLE = "botRepliesHitSample";
    public static final String BOT_REPLIES_UTILITY_PHRASE = "botRepliesUtilityPhrase";
    public static final String BOT_REPLIES_HIT_QA_PAIR_ID = "botRepliesHitQAPairId";
    public static final String BOT_REPLIES_HIT_QA_TAG = "botRepliesHitQATag";
    public static final String BOT_REPLIES_RICH_CARD = "botRepliesRichCard";
    public static final String BOT_REPLIES_CHANGE_APP = "botRepliesChangeApp";
    public static final String BOT_REPLIES_PASS_TO_LIVE_AGENT = "botRepliesPassToLiveAgent";
    public static final String BOT_REPLIES_CTX_FORM = "botRepliesCtxFrom";
    public static final String BOT_REPLIES_DROPPED_FORM = "botRepliesDroppedForm";
    public static final String BOT_REPLIES_UNANSWERED = "botRepliesUnanswered";
    public static final String BOT_REPLIES_IN_FORM = "botRepliesInForm";
    public static final String BOT_HANDLES_FORM = "botHandlesForm";
    public static final String BOT_REPLIES_SEND_EMAIL = "botRepliesSendEmail";
    public static final String BOT_REPLIES_INVALIDATE_LINK = "botRepliesInvalidateLink";
    public static final String BOT_REPLIES_INVALID_LINK = "botRepliesInvalidLink";
    public static final String BOT_REPLIES_IN_QUIZ = "botRepliesInQuiz";
    public static final String BOT_REPLIES_OBJECT = "botRepliesObject";
    public static final String TIMESTAMP_MS = "timestamp_ms";
    public static final String TURN_TIMESTAMP_MS = "turn_timestamp_ms";
    public static final String TURN_NO = "turn_no";

    public static final String BOT_REPLIES_POPUP_WIDGET = "popup_widget";

    // Alexa Card Keys
    public static final String ALEXA_CARD_TITLE_SUFFIX = "cardTitle";
    public static final String ALEXA_CARD_TEXT_SUFFIX = "cardText";
    public static final String ALEXA_CARD_IMAGE_URL_SMALL_SUFFIX = "cardImageSmall";
    public static final String ALEXA_CARD_IMAGE_URL_LARGE_SUFFIX = "cardImageLarge";
    // Echo Show Keys
    public static final String ECHO_SHOW_BODY_TITLE = "echo_show_body_title";
    public static final String ECHO_SHOW_BODY_SMALL_SOURCE_URL = "echo_show_body_sml_src_url";
    public static final String ECHO_SHOW_BODY_LARGE_SOURCE_URL = "echo_show_body_lrg_src_url";
    public static final String ECHO_SHOW_BODY_TEXT_CONTENT_TITLE = "echo_show_body_txt_cont_title";
    public static final String ECHO_SHOW_BODY_TEXT_CONTENT_SUBTITLE =
        "echo_show_body_txt_cont_subTitle";
    public static final String ECHO_SHOW_BODY_TEXT_CONTENT_PRIMARY_TEXT =
        "echo_show_body_txt_cont_primaryText";
    public static final String ECHO_SHOW_LIST_TITLE = "echo_show_list_title";
    public static final String ECHO_SHOW_LIST_OPTION1 = "echo_show_list_option1";
    public static final String ECHO_SHOW_LIST_OPTION2 = "echo_show_list_option2";
    public static final String ECHO_SHOW_LIST_OPTION3 = "echo_show_list_option3";
    public static final String ECHO_SHOW_LIST_OPTION4 = "echo_show_list_option4";

    public static final String TEXT = "text";
    public static final String VOICE = "voice";
    public static final String IMAGE = "image";
    public static final String VIDEO = "video";
    public static final String EMAIL = "email";
    public static final String SEARCH = "search";
    public static final String STRUCTURED_SEARCH = "structured_search";
    public static final String RECOMMEND = "recommend";
    public static final String INTENT_RECOMMEND = "intent_recommend";
    public static final String GOOGLE_CARD = "google_card";
    public static final String ALEXA_CARD = "alexa_card";
    public static final String ECHO_CARD = "echo_card";
    public static final String RICH_CARDS = "rich_cards";
    public static final String ECHO_SHOW = "echo_show";
    public static final String RICH_INPUT = "rich_input";

  }
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class TelephonyConstants {
    public static final Map<String, com.twilio.twiml.voice.Gather.Language> gatherLanguage =
        ImmutableMap.<String, com.twilio.twiml.voice.Gather.Language>builder()
            .put("hi-IN", com.twilio.twiml.voice.Gather.Language.HI_IN)
            .put("en-US", com.twilio.twiml.voice.Gather.Language.EN_US)
            .put("en-IN", com.twilio.twiml.voice.Gather.Language.EN_IN)
            .put("es-ES", com.twilio.twiml.voice.Gather.Language.ES_ES)
            .put("es-MX", com.twilio.twiml.voice.Gather.Language.ES_MX)
            .put("es-US", com.twilio.twiml.voice.Gather.Language.ES_US).build();

    public static final Map<String, com.twilio.twiml.voice.Say.Language> sayLanguage =
        ImmutableMap.<String, com.twilio.twiml.voice.Say.Language>builder()
            .put("hi-IN", com.twilio.twiml.voice.Say.Language.HI_IN)
            .put("en-US", com.twilio.twiml.voice.Say.Language.EN_US)
            .put("en-IN", com.twilio.twiml.voice.Say.Language.EN_IN)
            .put("es-ES", com.twilio.twiml.voice.Say.Language.ES_ES)
            .put("es-MX", com.twilio.twiml.voice.Say.Language.ES_MX)
            .put("es-US", com.twilio.twiml.voice.Say.Language.ES_US).build();

    public static final Map<String, Say.Voice> voice =
        ImmutableMap.<String, Say.Voice>builder().put(Say.Voice.MAN.toString(), Say.Voice.MAN)
            .put(Say.Voice.WOMAN.toString(), Say.Voice.WOMAN)
            .put(Say.Voice.ALICE.toString(), Say.Voice.ALICE)
            .put(Say.Voice.POLLY_JOANNA.toString(), Say.Voice.POLLY_JOANNA)
            .put(Say.Voice.POLLY_JOANNA_NEURAL.toString(), Say.Voice.POLLY_JOANNA_NEURAL)
            .put(Say.Voice.POLLY_CONCHITA.toString(), Say.Voice.POLLY_CONCHITA)
            .put(Say.Voice.POLLY_ENRIQUE.toString(), Say.Voice.POLLY_ENRIQUE)
            .put(Say.Voice.POLLY_ADITI.toString(), Say.Voice.POLLY_ADITI)
            .put(Say.Voice.POLLY_RAVEENA.toString(), Say.Voice.POLLY_RAVEENA)
            .put(Say.Voice.POLLY_PENELOPE.toString(), Say.Voice.POLLY_PENELOPE)
            .put(Say.Voice.POLLY_MIGUEL.toString(), Say.Voice.POLLY_MIGUEL)
            .put(Say.Voice.POLLY_LUPE_NEURAL.toString(), Say.Voice.POLLY_LUPE_NEURAL).build();

    public static final Map<String, String> NuralVoice =
        ImmutableMap.<String, String>builder().put(Say.Voice.MAN.toString(), "man")
            .put(Say.Voice.WOMAN.toString(), "woman").put(Say.Voice.ALICE.toString(), "alice")
            .put(Say.Voice.POLLY_JOANNA.toString(), "Polly.Joanna")
            .put(Say.Voice.POLLY_JOANNA_NEURAL.toString(), "Polly.Joanna")
            .put(Say.Voice.POLLY_CONCHITA.toString(), "Polly.Conchita")
            .put(Say.Voice.POLLY_ENRIQUE.toString(), "Polly.Enrique")
            .put(Say.Voice.POLLY_ADITI.toString(), "Polly.Aditi")
            .put(Say.Voice.POLLY_RAVEENA.toString(), "Polly.Raveena")
            .put(Say.Voice.POLLY_PENELOPE.toString(), "Polly.Penelope")
            .put(Say.Voice.POLLY_MIGUEL.toString(), "Polly.Miguel")
            .put(Say.Voice.POLLY_LUPE_NEURAL.toString(), "Polly.Lupe")
            .put("en-US-Standard-A", "Google.en-US-Standard-A")
            .put("en-US-Standard-B", "Google.en-US-Standard-B")
            .put("en-US-Standard-C", "Google.en-US-Standard-C")
            .put("en-US-Standard-D", "Google.en-US-Standard-D")
            .put("en-US-Standard-E", "Google.en-US-Standard-E")
            .put("en-US-Standard-F", "Google.en-US-Standard-F")
            .put("en-US-Standard-G", "Google.en-US-Standard-G")
            .put("en-US-Standard-H", "Google.en-US-Standard-H")
            .put("en-US-Standard-I", "Google.en-US-Standard-I")
            .put("en-US-Standard-J", "Google.en-US-Standard-J")
            .put("en-US-Wavenet-A", "Google.en-US-Wavenet-A")
            .put("en-US-Wavenet-B", "Google.en-US-Wavenet-B")
            .put("en-US-Wavenet-C", "Google.en-US-Wavenet-C")
            .put("en-US-Wavenet-D", "Google.en-US-Wavenet-D")
            .put("en-US-Wavenet-E", "Google.en-US-Wavenet-E")
            .put("en-US-Wavenet-F", "Google.en-US-Wavenet-F")
            .put("en-US-Wavenet-G", "Google.en-US-Wavenet-G")
            .put("en-US-Wavenet-H", "Google.en-US-Wavenet-H")
            .put("en-US-Wavenet-I", "Google.en-US-Wavenet-I")
            .put("en-US-Wavenet-J", "Google.en-US-Wavenet-J")
            .put("en-US-Neural2-A", "Google.en-US-Neural2-A")
            .put("en-US-Neural2-B", "Google.en-US-Neural2-B")
            .put("en-US-Neural2-C", "Google.en-US-Neural2-C")
            .put("en-US-Neural2-D", "Google.en-US-Neural2-D")
            .put("en-US-Neural2-E", "Google.en-US-Neural2-E")
            .put("en-US-Neural2-F", "Google.en-US-Neural2-F")
            .put("en-US-Neural2-G", "Google.en-US-Neural2-G")
            .put("en-US-Neural2-H", "Google.en-US-Neural2-H")
            .put("en-US-Neural2-I", "Google.en-US-Neural2-I")
            .put("en-US-Neural2-J", "Google.en-US-Neural2-J").build();

    public static final Map<String, TextToSpeechLanguage> textSpeechLanguage = ImmutableMap
        .<String, TextToSpeechLanguage>builder()
        .put(TextToSpeechLanguage.AMERICAN_ENGLISH.name(), TextToSpeechLanguage.AMERICAN_ENGLISH)
        .put(TextToSpeechLanguage.AMERICAN_SPANISH.name(), TextToSpeechLanguage.AMERICAN_SPANISH)
        .put(TextToSpeechLanguage.INDIAN_ENGLISH.name(), TextToSpeechLanguage.INDIAN_ENGLISH)
        .build();

    public static final Map<String, com.vonage.client.voice.ncco.SpeechSettings.Language> speechLanguage =
        ImmutableMap.<String, com.vonage.client.voice.ncco.SpeechSettings.Language>builder()
            .put(com.vonage.client.voice.ncco.SpeechSettings.Language.ENGLISH_UNITED_STATES.name(),
                com.vonage.client.voice.ncco.SpeechSettings.Language.ENGLISH_UNITED_STATES)
            .put(com.vonage.client.voice.ncco.SpeechSettings.Language.SPANISH_UNITED_STATES.name(),
                com.vonage.client.voice.ncco.SpeechSettings.Language.SPANISH_UNITED_STATES)
            .put(com.vonage.client.voice.ncco.SpeechSettings.Language.ENGLISH_INDIA.name(),
                com.vonage.client.voice.ncco.SpeechSettings.Language.ENGLISH_INDIA)
            .build();

    public static final Set<String> languageSet =
        ImmutableSet.of("hi-IN", "en-US", "en-IN", "es-ES", "es-MX");

  }

}
