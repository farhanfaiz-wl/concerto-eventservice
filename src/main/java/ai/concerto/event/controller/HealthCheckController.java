package ai.concerto.event.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ai.concerto.event.dto.HealthCheckModel.Health;
import ai.concerto.event.service.HealthCheckService;


@RestController
@RequestMapping("/health")
public class HealthCheckController {

  @Autowired
  private HealthCheckService healthCheckService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Health> healthCheck() throws Exception {
    return ResponseEntity.ok(healthCheckService.healthCheck());

  }
}
