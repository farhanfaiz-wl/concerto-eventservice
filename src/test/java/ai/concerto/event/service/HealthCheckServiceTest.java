package ai.concerto.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import ai.concerto.event.dto.HealthCheckModel;
import ai.concerto.event.utils.RestUtils;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

class HealthCheckServiceTest {

  @InjectMocks
  @Resource
  HealthCheckService healthCheckService;

  @Mock
  RedisTemplate redisTemplate;
  @Mock
  RestUtils restUtils;
  @Mock
  RedisConnection redisConnection;
  @Mock
  RedisConnectionFactory redisConnectionFactory;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void healthyMethodCheck() {
    String methodName = "methodName";
    String outPut = "output";
    HealthCheckModel.HealthService healthService = healthCheckService.healthy(methodName, outPut);
    assertEquals(methodName, healthService.getChecker());
    assertEquals(outPut, healthService.getOutput());
    assertEquals(true, healthService.getPassed());

  }

  @Test
  void unhealthy() {
    HealthCheckModel.HealthService healthService =
        healthCheckService.unhealthy("methodName", "outPut");
    assertEquals("methodName", healthService.getChecker());
    assertEquals("outPut", healthService.getOutput());
    assertEquals(false, healthService.getPassed());
  }

  @Test
  void falconCheckException() throws Exception {
    when(restUtils.getRequest("null/health", null, HealthCheckModel.Health.class))
        .thenThrow(Exception.class);
    assertThrows(Exception.class, () -> healthCheckService.falconCheck());
  }

}
