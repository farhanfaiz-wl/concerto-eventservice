package ai.concerto.event.service;

import ai.concerto.event.dto.DERequest;
import ai.concerto.event.dto.Session;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.ChannelIntegration;
import ai.concerto.event.exchange.UserDetails;
import ai.concerto.event.notification.supplier.ChannelNotificationHandlerSupplier;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class SessionService {

  private static final String SESSION_TIMEOUT_KEY_FORMATTER = "evs::session_time_out:%s";
  private static final String SESSION_KEY_PREFIX = "evs:session::";

  @Autowired
  private RedisTemplate redisTemplate;
  @Autowired
  private ObjectMapper snakeMaster;
  @Autowired
  private IntegrationService integrationService;
  @Autowired
  private AnalyticsService analyticsService;
  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;
  @Autowired
  private ChannelNotificationHandlerSupplier channelNotificationHandlerSupplier;

  @Cacheable(value = "session", key = "{#channel.name(), #universalUserId, #applicationId}")
  public Session getSession(String applicationId, String universalUserId, Channel channel) {
    return new Session(UUID.randomUUID().toString());
  }

  @CachePut(value = "session",
      key = "{#channel.name(), #deRequest.getUniversalUserId(), #deRequest.getProjectId()}")
  public Session updateSession(DERequest deRequest, Channel channel, Session session) {
    return session;
  }

  @CacheEvict(value = "session", key = "{#channel.name(), #universalUserId, #applicationId}")
  public void clearSession(String applicationId, String universalUserId, Channel channel,
      DERequest deRequest) {
    String key =
        SESSION_KEY_PREFIX.concat(Arrays.asList(channel.name(), universalUserId, applicationId)
            .stream().collect(Collectors.joining(",")));

    try {
      if (deRequest.isForceNewSession() && deRequest.isLogMe()) {
        // publish session expiry to analytics to mark the ticket as closed
        Session session = null;
        Object sessionObject = redisTemplate.opsForValue().get(key);

        if (!ObjectUtils.isEmpty(sessionObject)) {
          session = snakeMaster.readValue(
              snakeMaster.convertValue(sessionObject, new TypeReference<String>() {}),
              snakeMaster.getTypeFactory().constructType(new TypeReference<Session>() {}));

          analyticsStreamPublisher.publishAbandonedResponsesAnalytics(applicationId,
              channel.getName(), session.getUserId(), universalUserId, session.getSessionId(),
              session.isInsideForm(), session.isInsideQuiz());

          // get the new ticket number if chat is reset
          UserDetails userDetails =
              analyticsService.getUserDetails(deRequest.getProjectId(), deRequest.getUserId(),
                  deRequest.getSource().name(), deRequest.getTenantId(), deRequest);
          deRequest.setTicketId(userDetails.getTicket().getId());
          Optional.of(userDetails.getTicket().getNumber())
              .ifPresent(number -> deRequest.setTicketNumber(Integer.valueOf(number)));
        }
      }
    } catch (Exception ex) {
      log.error("Unable to deserialize session with key {} due to {}", key, ex);
    }
  }

  public void updateSessionTtl(String key, Integer timeOut) {
    if (timeOut == null) {
      timeOut = 1800;
    }
    redisTemplate.expire(key, timeOut, TimeUnit.SECONDS);
  }

  public Long getSessionTtl(String key) {
    return redisTemplate.getExpire(key, TimeUnit.SECONDS);
  }

  @SuppressWarnings("unchecked")
  @Scheduled(fixedRate = 30000)
  public void sessionTimeoutChecker() {
    log.debug("Running session timeout checker");
    String pattern = "evs:session::*";
    Map<String, Session> sessionMap = getAllRedisKeysbyPattern(pattern);
    if (!sessionMap.isEmpty()) {
      sessionMap.entrySet().parallelStream().forEach(sessionEntry -> {
        String[] splitKeys = sessionEntry.getKey().split(",");
        String channel = splitKeys[0].split("::")[1];
        Boolean timeOutMessageSent = isSessionTimeOutMessageSent(sessionEntry.getValue());

        ApplicationIntegration integration =
            integrationService.getApplicationIntegration(splitKeys[2]);

        if (!ObjectUtils.isEmpty(sessionEntry.getValue())
            && (ObjectUtils.isEmpty(timeOutMessageSent) || !timeOutMessageSent)
            && !(channel.equals(Channel.TELEPHONY.name())
                || channel.equals(Channel.EMAIL.name()))) {

          try {
            ChannelIntegration channelIntegration = integrationService
                .getChannelIntegration(splitKeys[2], Channel.valueOf(channel), integration);
            String sessionTimeoutPrompt =
                "We have not received any message for some time. If you wish to start the conversation again, please type Home.";
            if (!ObjectUtils.isEmpty(channelIntegration)
                && StringUtils.hasText(channelIntegration.getSessionTimeoutPrompt())) {
              sessionTimeoutPrompt = channelIntegration.getSessionTimeoutPrompt();
            }
            channelNotificationHandlerSupplier.getChannelTimeOutHandler(Channel.valueOf(channel))
                .handleChannelResponse(splitKeys[2], Channel.valueOf(channel),
                    sessionEntry.getValue(), sessionTimeoutPrompt, integration);
          } catch (Exception e) {
            log.error("error occurred while handling channel: {}", e.getMessage());
          }

          // create a redis key which indicates that session time out message is sent
          redisTemplate.opsForValue().set(
              String.format(SESSION_TIMEOUT_KEY_FORMATTER, sessionEntry.getValue().getSessionId()),
              "true", 30, TimeUnit.SECONDS);

          analyticsStreamPublisher.publishAbandonedResponsesAnalytics(splitKeys[2],
              channel.toLowerCase(), sessionEntry.getValue().getUserId(), splitKeys[1],
              sessionEntry.getValue().getSessionId(), sessionEntry.getValue().isInsideForm(),
              sessionEntry.getValue().isInsideQuiz());
        }
      });
    }
  }

  public Map<String, Session> getAllRedisKeysbyPattern(String pattern) {
    Map<String, Session> sessionMap = new HashMap<>();
    try (RedisConnection redisConnection = redisTemplate.getConnectionFactory().getConnection()) {
      ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
      Cursor<byte[]> evsSessionKeys = redisConnection.scan(options);

      String key = null;
      while (evsSessionKeys.hasNext()) {
        key = new String(evsSessionKeys.next());
        if (getSessionTtl(key) <= 30) {
          Session session = getTimedOutSession(key);
          sessionMap.put(key, session);
        }
      }
    } catch (Exception e) {
      log.error("Error occurred while fetching list of key by pattern {}", e);
    }
    return sessionMap;
  }

  public Session getTimedOutSession(String key) {
    Session session = null;
    try {
      Object sessionObject = redisTemplate.opsForValue().get(key);
      if (!ObjectUtils.isEmpty(sessionObject)) {
        session = snakeMaster.readValue(
            snakeMaster.convertValue(sessionObject, new TypeReference<String>() {}),
            snakeMaster.getTypeFactory().constructType(new TypeReference<Session>() {}));
      }
    } catch (Exception ex) {
      log.error("Unable to deserialize session with key {} due to {}", key, ex);
    }
    return session;
  }

  private Boolean isSessionTimeOutMessageSent(Session session) {
    try {
      if (!ObjectUtils.isEmpty(session)) {
        Object timeOutRedisValue = redisTemplate.opsForValue()
            .get(String.format(SESSION_TIMEOUT_KEY_FORMATTER, session.getSessionId()));
        if (!ObjectUtils.isEmpty(timeOutRedisValue)) {
          return snakeMaster.convertValue(timeOutRedisValue, new TypeReference<Boolean>() {});
        }
      }

    } catch (Exception ex) {
      log.error("Unable to deserialize timeOutMessageSent for session {} due to {}",
          session.getSessionId(), ex);
    }
    return false;
  }
}
