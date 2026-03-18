package ai.concerto.event.handler.response;

import ai.concerto.event.exchange.BotGoogleBusinessMessageResponse;
import ai.concerto.event.exchange.MessageInfo;
import ai.concerto.event.handler.BotResponseHandler;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.businessmessages.v1.Businessmessages;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesMessage;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesRepresentative;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesSuggestedReply;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesSuggestion;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class GoogleBusinessMessageResponseHandler implements BotResponseHandler {

  public static final String SLACK = "slack";

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;
  @Autowired
  private Businessmessages.Builder businesMessageBuilder;

  @SuppressWarnings("rawtypes")
  @Override
  public Object postBotResponse(Object response) {
    BotGoogleBusinessMessageResponse googleBusinessMessageResponse =
        (BotGoogleBusinessMessageResponse) response;
    try {
      // Create the text message
      BusinessMessagesMessage message =
          new BusinessMessagesMessage().setMessageId(UUID.randomUUID().toString())
              .setText(googleBusinessMessageResponse.getMessage())
              .setRepresentative(new BusinessMessagesRepresentative().setRepresentativeType("BOT"));

      if (!CollectionUtils.isEmpty(googleBusinessMessageResponse.getRecommendations())) {
        message.setSuggestions(
            googleBusinessMessageResponse.getRecommendations().stream().map(recommendation -> {
              String suggestion = recommendation.getOrDefault(SLACK, recommendation.get("text"));
              return new BusinessMessagesSuggestion().setReply(new BusinessMessagesSuggestedReply()
                  .setText(suggestion.length() > 25 ? suggestion.substring(0, 25) : suggestion)
                  .setPostbackData(recommendation.get("post_back")));
            }).toList());
      }

      // Create message request
      Businessmessages.Conversations.Messages.Create messageRequest =
          businesMessageBuilder.build().conversations().messages().create(
              "conversations/" + googleBusinessMessageResponse.getConversationId(), message);

      // Setup retries with exponential backoff
      HttpRequest httpRequest = ((AbstractGoogleClientRequest) messageRequest).buildHttpRequest();
      httpRequest.setUnsuccessfulResponseHandler(
          new HttpBackOffUnsuccessfulResponseHandler(new ExponentialBackOff()));
      httpRequest.execute();
    } catch (Exception e) {
      log.error("Unable to send google business message ,{}", e);
      analyticsStreamPublisher.publishErrorLogssAnalytics(googleBusinessMessageResponse,
          e.getMessage());
    }

    return response;
  }

  @Override
  public MessageInfo getMessageInfo(Object response) {
    // TODO Auto-generated method stub
    return new MessageInfo();
  }
}
