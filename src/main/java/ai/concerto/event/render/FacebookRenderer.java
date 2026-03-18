package ai.concerto.event.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import com.google.common.collect.Lists;
import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.FacebookIntegration;
import ai.concerto.event.exchange.BotFacebookResponse;
import ai.concerto.event.exchange.BotFacebookResponse.FbReplyQuickreplies;
import ai.concerto.event.handler.response.FacebookResponseHandler;
import ai.concerto.event.service.IntegrationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FacebookRenderer implements ResponseRenderer {

  @Autowired
  private FacebookResponseHandler facebookResponseHandler;

  @Autowired
  private IntegrationService integrationService;

  @Override
  public Object render(Object userRequest, DERequest deRequest, DEBotResponse deResponse) {
    BotFacebookResponse response = new BotFacebookResponse();

    String passToLiveAgent = deResponse.getPassToLiveAgent();
    boolean isLiveAgent = (passToLiveAgent != null && passToLiveAgent.equalsIgnoreCase("true"));
    ApplicationIntegration integration =
        integrationService.getApplicationIntegration(deRequest.getProjectId());
    FacebookIntegration facebookIntegration = (FacebookIntegration) integrationService
        .getChannelIntegration(deRequest.getProjectId(), Channel.FACEBOOK, integration);

    StringBuilder textBuilder = new StringBuilder();
    deResponse.getBotReply().getText().stream()
        .forEach(text -> textBuilder.append(text.trim()).append(System.lineSeparator()));

    String singleReply = textBuilder.toString();
    // add recommendation

    List<Map<String, String>> recommendRcmdMap = Lists.newArrayList();
    List<Map<String, String>> recommendIntentRcmdMap = Lists.newArrayList();
    List<Map<String, String>> recommendMap = Lists.newArrayList();

    if (!ObjectUtils.isEmpty(deResponse.getBotReply().getRecommend())
        && !deResponse.getBotReply().getRecommend().isEmpty()) {
      recommendRcmdMap = deResponse.getBotReply().getRecommend();
    }

    if (!ObjectUtils.isEmpty(deResponse.getBotReply().getIntentRecommend())
        && !deResponse.getBotReply().getIntentRecommend().isEmpty()) {
      recommendIntentRcmdMap = deResponse.getBotReply().getIntentRecommend();
    }

    // for facebook send 5 recommendations.
    Integer intentSize = 0;
    if (!ObjectUtils.isEmpty(recommendIntentRcmdMap) || !recommendIntentRcmdMap.isEmpty()) {
      intentSize = recommendIntentRcmdMap.size();
      if (intentSize > 5) {
        intentSize = 5;
      }
    }

    if (!ObjectUtils.isEmpty(recommendRcmdMap) && !recommendRcmdMap.isEmpty()) {
      for (int i = 0; i < 5 - intentSize && i < recommendRcmdMap.size(); i++) {
        recommendMap.add(recommendRcmdMap.get(i));
      }
    }

    for (int i = 0; i < intentSize; i++) {
      log.debug("adding intent rcmds to existing ones : {}",
          recommendIntentRcmdMap.get(i).toString());
      recommendMap.add(recommendIntentRcmdMap.get(i));
    }

    if (!isLiveAgent && !recommendMap.isEmpty()) {
      List<FbReplyQuickreplies> quickReplies = new ArrayList<>();
      for (Map<String, String> rcmMap : recommendMap) {
        quickReplies.add(processFbQuickReplies(rcmMap));
      }
      response.getMessage().setQuickReplies(quickReplies);
    }

    response.getRecipient().setId(deRequest.getUserId());
    response.getMessage().setText(singleReply);
    response.getMessage().setMetadata("DEVELOPER_DEFINED_METADATA");

    if (isLiveAgent) {
      log.debug("We are passing the handle to the live agent now! ");
      try {
        facebookResponseHandler.giveControlToLiveAgent(deRequest.getUserId(),
            deRequest.getProjectId(), facebookIntegration.getSecondaryAppId(), response);
        return null;
      } catch (Exception err) {
        log.error("Live agent pass-over failed {}", err);

      }
    }

    return response;
  }

  private FbReplyQuickreplies processFbQuickReplies(Map<String, String> rcmMap) {
    FbReplyQuickreplies fbReplyQuickreplies = new FbReplyQuickreplies();

    fbReplyQuickreplies.setContent_type("text");
    fbReplyQuickreplies.setTitle(rcmMap.getOrDefault("facebook", rcmMap.get("text")));
    fbReplyQuickreplies.setPayload(rcmMap.get("post_back"));
    // Assembling full fb-response

    return fbReplyQuickreplies;
  }

}
