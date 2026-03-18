package ai.concerto.event.controller;

import ai.concerto.event.enums.Channel;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.BookingWidgetIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.BookingWidgetsPreIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.ChannelIntegration;
import ai.concerto.event.exchange.BookingWidgetConfig;
import ai.concerto.event.service.FalconService;
import ai.concerto.event.service.IntegrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/event/widget")
public class BookingWidgetController {

  @Autowired
  private FalconService falconService;

  @Autowired
  private IntegrationService integrationService;

  @Autowired
  @Qualifier("objectMapper")
  private ObjectMapper objectMapper;

  private static final String PROJECT_ID = "project_id";
  private static final String RESPONSE_TIME = "response_time";
  private static final String REQUEST_TIMESTAMP_MS = "request_timestamp_ms";


  @SneakyThrows
  @GetMapping(path = "/{applicationId}/preIntegration", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BookingWidgetsPreIntegration> getChatBotPreIntegration(
      @PathVariable("applicationId") String applicationId) {
    MDC.put(PROJECT_ID, applicationId);
    log.debug("Rest GET API /event/widget/{}/preIntegration triggered", applicationId);
    ApplicationIntegration integration =
        integrationService.getApplicationIntegration(applicationId);
    MDC.put(RESPONSE_TIME, String.valueOf(
        System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP_MS))));
    log.info("Rest GET API /event/widget/{}/preIntegration processed", applicationId);

    ChannelIntegration bookingWidgetIntegration = integrationService
        .getChannelIntegration(applicationId, Channel.BOOKING_WIDGET, integration);
    if (!ObjectUtils.isEmpty(bookingWidgetIntegration)) {
      return ResponseEntity
          .ok(((BookingWidgetIntegration) bookingWidgetIntegration).getWidgetsPreIntegration());
    } else {
      return ResponseEntity.ok(new BookingWidgetsPreIntegration());
    }
  }

  @SneakyThrows
  @GetMapping(path = "/{widgetId}/config", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BookingWidgetConfig> getChatBotPreIntegratiosn(
      @PathVariable("widgetId") String widgetId) {
    log.debug("Rest GET API /event/widget/{}/config triggered", widgetId);
    MDC.put(RESPONSE_TIME, String.valueOf(
        System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP_MS))));
    log.info("Rest GET API /event/widget/{}/config processed", widgetId);
    return ResponseEntity.ok(falconService.getBookingWidgetConfig(widgetId));
  }
}
