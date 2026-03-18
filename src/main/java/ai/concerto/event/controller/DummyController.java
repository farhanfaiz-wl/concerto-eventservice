package ai.concerto.event.controller;

import ai.concerto.event.auth.service.TokenService;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/dummy")
public class DummyController {

  @Autowired
  private TokenService tokenService;

  @PostMapping("/token/inbox/api/{clientName}")
  public String generateApiToken(@PathVariable String clientName) {

    return tokenService.generateApiToken(clientName);
  }

  @PostMapping("/defaultToken")
  public void addApiToken(@RequestBody Map<String, String> redisMap) {
    tokenService.addRedisToken(redisMap);
  }

  @PostMapping("/webhook")
  public void consumeEvent(@RequestBody Map<String, Object> event) {
    log.info("Received web hook event: {}", event);
  }
}
