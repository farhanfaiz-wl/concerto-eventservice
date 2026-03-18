package ai.concerto.event.config;

import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.exchange.BotChatBotResponse;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

  @Bean
  public ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE)
        .setAmbiguityIgnored(true);

    return modelMapper;
  }

  @Bean
  public TypeMap<DEBotResponse, BotChatBotResponse> chatBotResponseTypeMap(
      ModelMapper modelMapper) {
    TypeMap<DEBotResponse, BotChatBotResponse> chatBotResponseTypeMap =
        modelMapper.typeMap(DEBotResponse.class, BotChatBotResponse.class);
    chatBotResponseTypeMap.addMapping(src -> src.getBotReply().getFormatted(),
        BotChatBotResponse::setBotReplies);
    chatBotResponseTypeMap.addMapping(src -> src.getBotReply().getRecommend(),
        BotChatBotResponse::setBotRepliesRcmd);
    chatBotResponseTypeMap.addMapping(src -> src.isIntentRecommend(),
        BotChatBotResponse::setIntentRcmd);
    chatBotResponseTypeMap.addMapping(src -> src.getBotReply().getRichCards(),
        BotChatBotResponse::setBotRepliesRichCard);
    chatBotResponseTypeMap.addMapping(src -> src.getBotReply().getImage(),
        BotChatBotResponse::setBotRepliesImage);
    chatBotResponseTypeMap.addMapping(src -> src.getBotReply().getVideo(),
        BotChatBotResponse::setBotRepliesVideo);

    return chatBotResponseTypeMap;
  }
}
