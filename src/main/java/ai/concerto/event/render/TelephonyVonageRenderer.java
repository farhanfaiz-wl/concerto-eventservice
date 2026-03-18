package ai.concerto.event.render;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vonage.client.voice.ncco.EventMethod;
import com.vonage.client.voice.ncco.InputAction;
import com.vonage.client.voice.ncco.Ncco;
import com.vonage.client.voice.ncco.SpeechSettings;
import com.vonage.client.voice.ncco.TalkAction;
import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.dto.Session;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.service.SessionService;
import ai.concerto.event.service.TelephonyService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TelephonyVonageRenderer {

  @Autowired
  private RedisTemplate redisTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private SessionService sessionService;

  @Autowired
  private TelephonyService telephonyService;

  private static final String SESSION_KEY_PREFIX = "evs:session::";

  public String render(DERequest deRequest, DEBotResponse deResponse, String language,
      Integer style, Integer maxDuration, Integer endOnSilence, StringBuilder uriBuf) {

    String response = null;
    if (!ObjectUtils.isEmpty(deResponse)) {

      String botreply = "";
      if (!CollectionUtils.isEmpty(deResponse.getBotReply().getText())) {
        StringBuilder textBuilder = new StringBuilder();
        deResponse.getBotReply().getText().stream()
            .forEach(text -> textBuilder.append(text.trim()).append(System.lineSeparator()));
        botreply = textBuilder.toString();
      }
      if (botreply.endsWith(".")) {
        // remove the last character
        botreply = botreply.substring(0, botreply.length() - 1);
      }
      boolean isprocessUserUtternace = false;
      // hack to process the alphanumeric character
      if (botreply.contains("client_api_user_utterance_process")) {

        botreply = botreply.replace("client_api_user_utterance_process", "");
        isprocessUserUtternace = true;
      }

      String sessionKey = SESSION_KEY_PREFIX
          .concat(Arrays.asList(Channel.TELEPHONY.name(), deRequest.getUniversalUserId(),
              deRequest.getProjectId()).stream().collect(Collectors.joining(",")));
      Session session = objectMapper.convertValue(redisTemplate.opsForValue().get(sessionKey),
          new TypeReference<Session>() {});
      session.setProcessUserUtternace(isprocessUserUtternace);
      sessionService.updateSession(deRequest, Channel.TELEPHONY, session);
      sessionService.updateSessionTtl(sessionKey, 1800);

      StringBuilder message = new StringBuilder();
      message.append(botreply).append(System.lineSeparator());
      TalkAction talkAction = new TalkAction.Builder(message.toString())
          .language(telephonyService.getTextSpeachLanguage(language)).bargeIn(true)
          .level(Float.valueOf(1)).style(style).build();
      SpeechSettings speechSettings = new SpeechSettings();
      speechSettings.setLanguage(telephonyService.getSpeachLanguage(language));
      speechSettings.setEndOnSilence(endOnSilence);
      speechSettings.setMaxDuration(maxDuration);
      speechSettings.setContext(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
          "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "1", "2", "3",
          "4", "5", "6", "7", "8", "9", "0"));

      InputAction inputAction =
          new InputAction.Builder().eventMethod(EventMethod.POST).eventUrl(uriBuf.toString())
              .type(Collections.singletonList("speech")).speech(speechSettings).build();
      response = new Ncco(Arrays.asList(talkAction, inputAction)).toJson();
      MDC.put("response_time",
          String.valueOf(System.currentTimeMillis() - Long.parseLong(MDC.get("request_time"))));
      log.info("Vonage voice event response generated successfully");
    }
    return response;
  }
}
