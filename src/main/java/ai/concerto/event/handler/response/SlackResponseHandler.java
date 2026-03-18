package ai.concerto.event.handler.response;

import ai.concerto.event.exchange.BotSlackResponse;
import ai.concerto.event.exchange.MessageInfo;
import ai.concerto.event.handler.BotResponseHandler;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Slf4j
@Service
public class SlackResponseHandler implements BotResponseHandler {

  @Autowired
  private ObjectMapper snakeCaseMapper;

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Override
  public Object postBotResponse(Object response) {
    BotSlackResponse slackResponse = (BotSlackResponse) response;
    try {
      ChatPostMessageResponse postMessageResponse =
          Slack.getInstance().methods(slackResponse.getToken())
              .chatPostMessage(req -> req.channel(slackResponse.getChannel())
                  .text(slackResponse.getText()).unfurlLinks(slackResponse.getUnfurlLinks())
                  .unfurlMedia(slackResponse.getUnfurlMedia()));

      if (!ObjectUtils.isEmpty(postMessageResponse.getError())) {
        log.error("Posting message to slack failed due to error:{}. Full error: {}",
            postMessageResponse.getError(), postMessageResponse);
        analyticsStreamPublisher.publishErrorLogssAnalytics(slackResponse,
            postMessageResponse.getError());
      }

      addResponseTime();
      log.info("SuccessFully posted the message to slack");
    } catch (SlackApiException | IOException e) {
      log.error("Unable to send slack response", e);
      analyticsStreamPublisher.publishErrorLogssAnalytics(slackResponse, e.getMessage());
    }
    return response;
  }

  @Override
  public MessageInfo getMessageInfo(Object response) {
    // TODO Auto-generated method stub
    return new MessageInfo();
  }
}
