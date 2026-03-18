package ai.concerto.event.handler.request;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.dto.Session;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.ClientType;
import ai.concerto.event.enums.Source;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.UserDetails;
import ai.concerto.event.service.AnalyticsService;
import ai.concerto.event.service.IntegrationService;
import ai.concerto.event.service.SessionService;
import ai.concerto.event.utils.PhoneUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TelephonyOzonetelRequestHandler {

  private static final String SESSION_KEY_PREFIX = "evs:session::";

  @Autowired
  private IntegrationService integrationService;

  @Autowired
  private AnalyticsService analyticsService;

  @Autowired
  private RedisTemplate redisTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private SessionService sessionService;

  public DERequest getDeRequest(String applicationId, String callStatus, String fromNumber,
      String data, String silentCount) throws Exception {

    DERequest deRequest = new DERequest();
    deRequest.setProjectId(applicationId);
    ApplicationIntegration integration =
        integrationService.getApplicationIntegration(applicationId);
    deRequest = integrationService.updateIntegrationDetails(deRequest, integration);
    deRequest.setTurnId(UUID.randomUUID().toString());
    String userId = "+91".concat(fromNumber.substring(1));
    deRequest.setUserId(userId);

    // setting universal_user_id
    UserDetails userDetails = analyticsService.getUserDetails(applicationId, deRequest.getUserId(),
        Source.telephony.name(), deRequest.getTenantId(),deRequest);

    deRequest.setUniversalUserId(userDetails.getUserProfile().getUniversalUserId());
    deRequest.setTicketId(userDetails.getTicket().getId());
    deRequest.setUserProfile(userDetails.getUserProfile());

    String sessionKey = SESSION_KEY_PREFIX.concat(Arrays
        .asList(Channel.TELEPHONY.name(), deRequest.getUniversalUserId(), deRequest.getProjectId())
        .stream().collect(Collectors.joining(",")));

    Session session = null;
    Object sessionStr = redisTemplate.opsForValue().get(sessionKey);
    if (callStatus.equals("NewCall") || ObjectUtils.isEmpty(sessionStr)) {
      deRequest.setUserInputLast("");
      deRequest.setSessionNew(true);
      session = sessionService.getSession(deRequest.getProjectId(), deRequest.getUniversalUserId(),
          Channel.TELEPHONY);
      session.setSilentCount(Integer.valueOf(silentCount));
      sessionService.updateSession(deRequest, Channel.TELEPHONY, session);
    } else if (callStatus.equals("Disconnect") || callStatus.equals("Hangup")) {
      sessionService.clearSession(deRequest.getProjectId(), deRequest.getUniversalUserId(),
          Channel.TELEPHONY, deRequest);
      return null;
    } else {
      session = objectMapper.readValue(sessionStr.toString(), Session.class);
      if (StringUtils.hasText(data)) {
        // update the silent count if the user input is not empty
        session.setSilentCount(Integer.valueOf(silentCount));
        sessionService.updateSession(deRequest, Channel.TELEPHONY, session);
      }
      deRequest.setUserInputLast(data);
      deRequest.setSessionNew(false);
    }
    deRequest.setSessionTimeOut(1800);
    deRequest.setSessionId(session.getSessionId());
    sessionService.updateSessionTtl(sessionKey, 1800);

    com.google.i18n.phonenumbers.Phonenumber.PhoneNumber phoneNumber =
        PhoneUtils.getPhoneNumber(deRequest.getUserId());

    deRequest.setPhoneNumberWithCountryCode(deRequest.getUserId());
    deRequest.setPhoneNumberCountryCode(String.valueOf(phoneNumber.getCountryCode()));
    deRequest.setPhoneNumber(String.valueOf(phoneNumber.getNationalNumber()));

    deRequest.setSource(Source.telephony);
    deRequest.setClientType(ClientType.VOICE.getName());
    deRequest.setSendSearchResults(Boolean.FALSE);
    deRequest.setQAEnable(Boolean.TRUE);

    return deRequest;

  }
}
