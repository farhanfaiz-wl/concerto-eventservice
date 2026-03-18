package ai.concerto.event.controller;

import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ai.concerto.event.dto.ProjectCloneRequest;
import ai.concerto.event.exchange.ProjectCloneResponse;
import ai.concerto.event.exchange.TenantCloneRequest;
import ai.concerto.event.exchange.TenantCloneResponse;
import ai.concerto.event.service.FalconService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v0/tenant")
public class DeveloperApiController {

  private static final String RESPONSE_TIME = "response_time";
  private static final String REQUEST_TIMESTAMP = "request_timestamp_ms";

  @Autowired
  private FalconService falconService;

  @PostMapping(path = "/{tenantId}/clone", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TenantCloneResponse> cloneTenant(@PathVariable String tenantId,
      @RequestBody TenantCloneRequest request) {
    TenantCloneResponse response = falconService.cloneTenant(tenantId, request);
    MDC.put(RESPONSE_TIME, String
        .valueOf(System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP))));
    log.info("Rest POST API /v0/tenant/create processed.");
    return ResponseEntity.ok(response);
  }

  @PostMapping(path = "/{tenantId}/projects/clone", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProjectCloneResponse> cloneProjects(@PathVariable String tenantId,
      @RequestBody ProjectCloneRequest request) {
    ProjectCloneResponse response = falconService.cloneProjects(tenantId, request);
    MDC.put(RESPONSE_TIME, String
        .valueOf(System.currentTimeMillis() - Long.valueOf((String) MDC.get(REQUEST_TIMESTAMP))));
    log.info("Rest POST API /v0/tenant/create processed.");
    return ResponseEntity.ok(response);
  }
}
