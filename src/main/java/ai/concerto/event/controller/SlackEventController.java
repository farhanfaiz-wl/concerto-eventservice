package ai.concerto.event.controller;

import java.util.Collections;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.exchange.SlackRequest;
import ai.concerto.event.publisher.UserRequestStreamPublisher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/event")
public class SlackEventController {


  @Autowired
  private UserRequestStreamPublisher userRequestStreamPublisher;

  @Autowired
  private ObjectMapper objectMapper;

  @PostMapping({"/slack/{applicationId}", "{applicationId}/slack"})
  public ResponseEntity processSlackRequest(@PathVariable String applicationId,
      @RequestBody String request) throws Exception {
    log.debug("Received slack request: {}", request);
    MDC.put("project_id", applicationId);
    MDC.put("request_time", System.currentTimeMillis());
    SlackRequest slackRequest =
        objectMapper.readValue(request, new TypeReference<SlackRequest>() {});
    if ("url_verification".equals(slackRequest.getType())) {
      return ResponseEntity.ok(Collections.singletonMap("challenge", slackRequest.getChallenge()));

    } else if ((!ObjectUtils.isEmpty(slackRequest.getAuthorizations())
        && !slackRequest.getAuthorizations().get(0).getIsBot())
        || StringUtils.hasText(slackRequest.getEvent().getBotId())
        || StringUtils.hasText(slackRequest.getEvent().getSubType())) {
      return ResponseEntity.ok().build();
    } else {
      userRequestStreamPublisher.publishUserRequest(applicationId, request, Channel.SLACK, null);
    }
    return ResponseEntity.ok().build();
  }
}
