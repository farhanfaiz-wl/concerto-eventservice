package ai.concerto.event.render;

import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.exchange.BotChatBotResponse;
import ai.concerto.event.exchange.TextToSpeechRequest;
import ai.concerto.event.service.AllegroUiApiService;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ChatBotRenderer implements ResponseRenderer {

  @Autowired
  private TypeMap<DEBotResponse, BotChatBotResponse> chatBotResponseTypeMap;

  @Autowired
  private AllegroUiApiService allegroUiApiService;

  @Override
  public Object render(Object userRequest, DERequest deRequest, DEBotResponse deResponse) {
    BotChatBotResponse response = chatBotResponseTypeMap.map(deResponse);
    response.setWebSocketUserName(deRequest.getWebSocketUserName());
    response.setProjectId(deRequest.getProjectId());
    response.setTurnId(deRequest.getTurnId());
    response.setUserId(deRequest.getUserId());

    if (response.isIntentRcmd()) {
      response.setBotRepliesRcmd(deResponse.getBotReply().getIntentRecommend().stream()
          .map(ir -> (HashMap<String, String>) ir).toList());
    }

    if (!CollectionUtils.isEmpty(deResponse.getBotReply().getAlexaCard())) {
      response.setBotRepliesCard(new HashMap<>());
      response.getBotRepliesCard().putAll(deResponse.getBotReply().getAlexaCard().get(0));
    }
    if (!CollectionUtils.isEmpty(deResponse.getBotReply().getGoogleCard())) {
      response.setBotRepliesGoogleCard(new HashMap<>());
      response.getBotRepliesGoogleCard().putAll(deResponse.getBotReply().getGoogleCard().get(0));
    }
    if (!CollectionUtils.isEmpty(deResponse.getBotReply().getVoice())) {
      response.setBotRepliesVoice(String.format("<speak>%s</speak>",
          deResponse.getBotReply().getVoice().stream().collect(Collectors.joining(","))));
    }
    if (!CollectionUtils.isEmpty(deResponse.getBotReply().getPopupWidget())) {
      response.setBotReplyPopUpWidget(new HashMap<>(deResponse.getBotReply().getPopupWidget()));
    }
    if (!CollectionUtils.isEmpty(deResponse.getBotReply().getRichInput())) {
      response.setBotRepliesRichInput(new HashMap<>(deResponse.getBotReply().getRichInput()));
    }

    if (deRequest.isChatbotVoice()) {
      TextToSpeechRequest textToSpeechRequest = new TextToSpeechRequest();
      textToSpeechRequest.getInput().setSsml(response.getBotRepliesVoice());
      textToSpeechRequest.getVoice().setLanguageCode(deRequest.getLanguage());
      textToSpeechRequest.getVoice().setName(deRequest.getVoiceName());
      textToSpeechRequest.getVoice().setSsmlGender(deRequest.getVoiceSsmlGender());
      response
          .setBotVoice(new HashMap<>(allegroUiApiService.convertTextToSpeech(textToSpeechRequest)));
    }
    return response;
  }
}
