package ai.concerto.event.service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ai.concerto.event.dto.HealthCheckModel.Health;
import ai.concerto.event.dto.HealthCheckModel.HealthService;
import ai.concerto.event.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class HealthCheckService {

  @Value("${spring.redis.url}")
  private String redisUrl;

  @Value("${service.analytics.uri}")
  private String analyticsUrl;

  @Value("${service.de.uri}")
  private String dgiUrl;

  @Value("${service.falcon.uri}")
  private String falconUrl;

  @Autowired
  private RestUtils restUtils;

  @Autowired
  private RedisTemplate redisTemplate;


  public HealthService healthy(String methodName, String output) {
    HealthService healthService = new HealthService();
    healthService.setChecker(methodName);
    healthService.setOutput(output);
    healthService.setPassed(true);
    healthService.setTimestamp(new Timestamp(System.currentTimeMillis()).getTime());
    return healthService;

  }

  public HealthService unhealthy(String methodName, String output) {
    HealthService healthService = new HealthService();
    healthService.setChecker(methodName);
    healthService.setOutput(output);
    healthService.setPassed(false);
    healthService.setTimestamp(new Timestamp(System.currentTimeMillis()).getTime());
    return healthService;

  }


  public HealthService redisCheck() throws Exception {

    try {
      String redisResponse = redisTemplate.getConnectionFactory().getConnection().ping();
    } catch (Exception e) {
      log.error("Exception in Redis Connection:", e);
      return unhealthy("redisCheck", "CONNECTION TO REDIS FAILED");
    }
    return healthy("redisCheck", "REDIS OK");
  }

  public HealthService dgiCheck() throws Exception {
    String methodName = Object.class.getEnclosingMethod().getName();
    try {
      HttpURLConnection dgiConnection = (HttpURLConnection) new URL(dgiUrl).openConnection();
      dgiConnection.setRequestMethod("GET");
      dgiConnection.connect();
      log.info("DGI Response code: {}, DGI Response Message: {}", dgiConnection.getResponseCode(),
          dgiConnection.getResponseMessage());
    } catch (Exception e) {
      log.error("Exception in DGI Connection:", e);
      return unhealthy(methodName, "CONNECTION TO DGI FAILED");
    }
    return healthy(methodName, "DGI OK");
  }

  public HealthService falconCheck() throws Exception {
    String methodName = Object.class.getEnclosingMethod().getName();
    Health response = new Health();
    String requestUri = String.format("%s/health", falconUrl);
    try {
      response = restUtils.getRequest(requestUri, null, Health.class);
      if (response.status.equals("success")) {
        return healthy(methodName, "FALCON OK");
      } else {
        log.info(response.toString());
      }
    } catch (Exception e) {
      log.error("Exception in Falcon Connection:", e);
      return unhealthy(methodName, "CONNECTION TO FALCON FAILED");
    }
    return unhealthy(methodName, "CONNECTION TO FALCON FAILED");
  }

  public HealthService analyticsCheck() throws Exception {
    String methodName = Object.class.getEnclosingMethod().getName();
    try {
      HttpURLConnection analyticsConnection =
          (HttpURLConnection) new URL(analyticsUrl).openConnection();
      analyticsConnection.setRequestMethod("GET");
      analyticsConnection.connect();
      log.info("Analytics Response code: {}, Analytics Response Message: {}",
          analyticsConnection.getResponseCode(), analyticsConnection.getResponseMessage());
    } catch (Exception e) {
      log.error("Exception in Analytics Connection:", e);
      return unhealthy(methodName, "CONNECTION TO ANALYTICS FAILED");
    }
    return healthy(methodName, "ANALYTICS OK");
  }

  public Health healthCheck() throws Exception {
    Health result = new Health();
    List<HealthService> results = new ArrayList<>();
    HealthService redisHealth = null;

    redisHealth = redisCheck();


    results.add(redisHealth);

    result.setResults(results);
    for (HealthService res : results) {
      if (Boolean.FALSE.equals(res.getPassed())) {
        result.setStatus("failed");
        break;
      } else
        result.setStatus("success");
    }

    result.setTimestamp(new Timestamp(System.currentTimeMillis()).getTime());
    return result;
  }

}
