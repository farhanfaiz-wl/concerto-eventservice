package ai.concerto.event.publisher;

import ai.concerto.event.enums.EventType;
import ai.concerto.event.exchange.Event;
import ai.concerto.event.exchange.EventSubscriptionDetails;
import ai.concerto.event.exchange.EventSubscriptionDetails.EventDetails;
import ai.concerto.event.service.FalconService;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebhookEventPublisher {

  @Autowired private RestUtils restUtils;

  @Autowired private FalconService falconService;

  @Autowired private ObjectMapper snakeCaseMapper;

  public void publishEvent(String applicationId, EventType eventType, Event event) {
    EventSubscriptionDetails subscriptionDetails =
        falconService.getApplicationEventSubscriptionDetails(applicationId);
    boolean subscribed = false;
    for (EventDetails eventDetails : subscriptionDetails.getSubscribedEvents()) {
      if (eventType.name().equalsIgnoreCase(eventDetails.getName())) {
        subscribed = true;
        break;
      }
    }
    if (!subscribed) {
      return;
    }

    String eventStr = event.data(snakeCaseMapper);
    try {
      restUtils.postRequest(subscriptionDetails.getUrl(), eventStr, null, String.class);
      log.debug(
          "Successfully published event of type: {} to webhook: {}",
          eventType,
          subscriptionDetails.getUrl());
    } catch (Exception e) {
      log.error(
          "Unable to publish event: {} to webhook: {}", eventStr, subscriptionDetails.getUrl(), e);
    }
  }
}
