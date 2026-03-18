package ai.concerto.event.controller;

import ai.concerto.event.enums.Channel;
import ai.concerto.event.service.UserRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/event/email")
public class EmailEventController {

  @Autowired
  private UserRequestService userRequestService;

  @Autowired
  private ObjectMapper objectMapper;

  @SneakyThrows
  @PostMapping(path = "/{applicationId}", produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public String emailWebhook(@PathVariable String applicationId,
      @RequestParam(value = "async", required = false) Boolean isAsync,
      @RequestHeader(value = "X-Forwarded-Host", required = false) String host,
      @RequestBody Map<String, String> request) throws ExecutionException {
    MDC.put("project_id", applicationId);
    log.debug("Received email request: {}", request);

    request.put("host", host);
    request.put("isAsync", isAsync.toString());

    Object response =
        userRequestService.processUserRequest(applicationId, request, Channel.EMAIL, null);

    return objectMapper.writeValueAsString(response);
  }

}
