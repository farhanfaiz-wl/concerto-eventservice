package ai.concerto.event.service;

import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.dto.Session;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.ControlOwner;
import ai.concerto.event.exchange.BotResponse;
import ai.concerto.event.exchange.UserDetails;
import ai.concerto.event.handler.UserRequestHandlerFactory;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.render.ResponseRendererFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class UserRequestService {

  private static final String SESSION_KEY_PREFIX = "evs:session::";

  private static final String MULTI_APP_KEY_PREFIX = "evs:multi_application:%s:%s:%s";


  @Autowired
  private AnalyticsService analyticsService;

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Autowired
  private UserRequestHandlerFactory handlerFactory;

  @Autowired
  private ResponseRendererFactory rendererFactory;

  @Autowired
  private DEService deService;

  @Autowired
  private SessionService sessionService;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ServiceDetails serviceDetails;

  @Autowired
  private MultiAppService multiAppService;

  public Object processUserRequest(String applicationId, Object request, Channel channel,
      String vendor) throws Exception {

    DERequest deRequest =
        handlerFactory.getUserRequestHandler(channel, vendor).getDeRequest(applicationId, request);
    // set project template in MDC
    MDC.put("template", deRequest.getTemplate());

    // set havean url in derequest
    deRequest.setBaseUrl(serviceDetails.getBase().getUri());

    // set logMe as true if application id belongs to any of the following projects
    if (Arrays
        .asList("655ca5f05dc7e40f724b513e", "6557494c5dc7e40f724b45dd", "6557498a5dc7e40f724b467c")
        .contains(applicationId)) {
      deRequest.setLogMe(true);
    }

    // setting universal_user_id
    if (deRequest.isLogMe()) {

      UserDetails userDetails = analyticsService.getUserDetails(deRequest.getProjectId(),
          deRequest.getUserId(), deRequest.getSource().name(), deRequest.getTenantId(), deRequest);
      deRequest.setUniversalUserId(userDetails.getUserProfile().getUniversalUserId());
      deRequest.setTicketId(userDetails.getTicket().getId());
      Optional.of(userDetails.getTicket().getNumber())
          .ifPresent(number -> deRequest.setTicketNumber(Integer.valueOf(number)));
      deRequest.setUserProfile(userDetails.getUserProfile());
      if (!StringUtils.hasText(deRequest.getUserFullName())) {
        deRequest.setUserFullName(userDetails.getUserProfile().getFullName());
      }

      if (deRequest.isLiveAgentRunning()
          || !ControlOwner.BOT.name().equals(userDetails.getControl().getOwner())) {
        // update session ttl on user message
        String sessionKey = SESSION_KEY_PREFIX.concat(
            Arrays.asList(channel.name(), deRequest.getUniversalUserId(), deRequest.getProjectId())
                .stream().collect(Collectors.joining(",")));
        sessionService.updateSessionTtl(sessionKey, deRequest.getSessionTimeOut());

        Map<String, Object> agentReply = new HashMap<>();
        agentReply.put("payload", getuserMessagePayload(deRequest, userDetails));
        analyticsStreamPublisher.publishAgentMessage(objectMapper.writeValueAsString(agentReply));
        return null;
      }
    } else {
      deRequest
          .setUniversalUserId(UUID.nameUUIDFromBytes(deRequest.getUserId().getBytes()).toString());
    }

    if (!StringUtils.hasText(deRequest.getTurnId()))
      deRequest.setTurnId(UUID.randomUUID().toString());

    if ((!StringUtils.hasText(deRequest.getUserInputLast()) && deRequest.isSessionNew())
        || deRequest.isForceNewSession())
      sessionService.clearSession(deRequest.getProjectId(), deRequest.getUniversalUserId(), channel,
          deRequest);

    Session session = sessionService.getSession(deRequest.getProjectId(),
        deRequest.getUniversalUserId(), channel);

    deRequest.setSessionId(session.getSessionId());
    deRequest.setSessionNew(session.isNew());

    // fetch the response from DE
    DEBotResponse deResponse = deService.postDeRequest(deRequest);

    if (StringUtils.hasText(deResponse.getChangeApp())) {
      return multiAppService.getMultiAppResponse(deResponse, deRequest, request, channel, vendor);
    }

    session.setNew(false);
    session.setInsideForm(deResponse.isInForm());
    session.setInsideQuiz(deResponse.isInQuiz());
    session.setSlackChannel(deRequest.getSlackChannel());
    session.setWhatsappPhoneNumberId(deRequest.getPhoneNumberId());
    session.setUserId(deRequest.getUserId());
    session.setWebSocketUserName(deRequest.getWebSocketUserName());
    sessionService.updateSession(deRequest, channel, session);

    String sessionKey = SESSION_KEY_PREFIX
        .concat(Arrays.asList(channel.name(), deRequest.getUniversalUserId(), applicationId)
            .stream().collect(Collectors.joining(",")));
    sessionService.updateSessionTtl(sessionKey, deRequest.getSessionTimeOut());

    BotResponse response = (BotResponse) rendererFactory.getResponseRenderer(channel, vendor)
        .render(request, deRequest, deResponse);

    response.setProjectId(deRequest.getProjectId());
    response.setTurnId(deRequest.getTurnId());
    response.setSessionId(deRequest.getSessionId());
    response.setUserId(deRequest.getUserId());
    response.setUniversalUserId(deRequest.getUniversalUserId());
    response.setSource(deRequest.getSource().name());

    return response;

  }

  private Map<String, Object> getuserMessagePayload(DERequest deRequest, UserDetails userDetails) {
    Map<String, Object> payLoad = new HashMap<>();
    Map<String, Object> message = new HashMap<>();
    Map<String, String> ticket = new HashMap<>();

    DateTimeFormatter sourceFormat =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'");
    DateTimeFormatter targetFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    LocalDateTime dateTime =
        LocalDateTime.parse(OffsetDateTime.now(ZoneOffset.UTC).toString(), sourceFormat);
    String formattedDateTime = dateTime.atZone(ZoneId.of("UTC")).format(targetFormat);

    message.put("text", deRequest.getUserInputLast());
    message.put("actor", "USER");
    message.put("type", "MESSAGE");
    message.put("time_stamp", formattedDateTime);
    ticket.put("id", userDetails.getTicket().getId());
    ticket.put("number", userDetails.getTicket().getNumber());

    payLoad.put("project_id", deRequest.getProjectId());
    payLoad.put("channel", userDetails.getChannel());
    payLoad.put("universal_user_id", deRequest.getUniversalUserId());
    payLoad.put("message", message);
    payLoad.put("ticket", ticket);

    return payLoad;

  }

}
