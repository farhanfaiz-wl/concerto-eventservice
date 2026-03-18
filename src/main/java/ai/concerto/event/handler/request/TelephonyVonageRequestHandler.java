package ai.concerto.event.handler.request;

import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vonage.client.incoming.InputEvent;
import com.vonage.client.incoming.Result;
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
public class TelephonyVonageRequestHandler {

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

  private static final String VOICE = "voice_";
  private static final String SESSION_KEY_PREFIX = "evs:session::";

  public DERequest getDeRequest(String applicationId, InputEvent event) throws Exception {
    DERequest deRequest = new DERequest();
    deRequest.setProjectId(applicationId);
    ApplicationIntegration integration =
        integrationService.getApplicationIntegration(applicationId);
    deRequest = integrationService.updateIntegrationDetails(deRequest, integration);

    deRequest.setTurnId(UUID.randomUUID().toString());

    String fromNumber = "+".concat(event.getFrom());
    String userId = VOICE.concat(fromNumber);
    com.google.i18n.phonenumbers.Phonenumber.PhoneNumber phoneNumber =
        PhoneUtils.getPhoneNumber(fromNumber);
    deRequest.setPhoneNumberWithCountryCode(fromNumber);
    deRequest.setPhoneNumberCountryCode(String.valueOf(phoneNumber.getCountryCode()));
    deRequest.setPhoneNumber(String.valueOf(phoneNumber.getNationalNumber()));
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

    // session management
    String userInputLast = null;
    Session session = null;
    if (!ObjectUtils.isEmpty(redisTemplate.opsForValue().get(sessionKey))) {
      session = objectMapper.convertValue(redisTemplate.opsForValue().get(sessionKey),
          new TypeReference<Session>() {});
      session.setNew(false);
    } else {
      session = sessionService.getSession(deRequest.getProjectId(), deRequest.getUniversalUserId(),
          Channel.TELEPHONY);
      session.setNew(true);
    }
    sessionService.updateSession(deRequest, Channel.TELEPHONY, session);
    sessionService.updateSessionTtl(sessionKey, 1800);

    if (ObjectUtils.isEmpty(event.getSpeech())) {
      deRequest.setUserInputLast("");
    } else {
      userInputLast = event.getSpeech().getResults().iterator().next().getText();
      if (session.isProcessUserUtternace()) {
        Iterator<Result> it = event.getSpeech().getResults().iterator();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
          sb.append(it.next().getText()).append(",");
        }
        userInputLast = sb.toString();
        userInputLast = userInputLast.substring(0, userInputLast.length() - 1);
      }
      deRequest.setUserInputLast(userInputLast);
    }

    deRequest.setSessionId(session.getSessionId());
    deRequest.setSessionNew(session.isNew());
    deRequest.setSource(Source.telephony);
    deRequest.setClientType(ClientType.VOICE.getName());
    deRequest.setSendSearchResults(Boolean.FALSE);
    deRequest.setQAEnable(Boolean.TRUE);
    return deRequest;
  }

}
