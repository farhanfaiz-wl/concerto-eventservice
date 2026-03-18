package ai.concerto.event.config;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import co.elastic.apm.attach.ElasticApmAttacher;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Configuration
@ConditionalOnProperty(value = "spring.elastic.apm.enabled", havingValue = "true")
public class ElasticApmConfig {

  public static final String SERVER_URL = "server_url";
  public static final String SECRET_TOKEN = "secret_token";
  public static final String SERVICE_NAME = "service_name";
  public static final String ENVIRONMENT = "environment";
  public static final String APPLICATION_PACKAGES = "application_packages";


  @Value("${spring.elastic.apm.server_url}")
  private String serverUrl;
  @Value("${spring.elastic.apm.secret_token}")
  private String secretToken;
  @Value("${spring.elastic.apm.environment}")
  private String env;

  private String applicationPackages = "ai.concerto.event";


  @PostConstruct
  public void init() {
    Map<String, String> apmProps = new HashMap<>(5);
    apmProps.put(SERVER_URL, serverUrl);
    apmProps.put(SECRET_TOKEN, secretToken);
    apmProps.put(ENVIRONMENT, env);
    apmProps.put(SERVICE_NAME, "eventservice");
    apmProps.put(APPLICATION_PACKAGES, applicationPackages);
    ElasticApmAttacher.attach(apmProps);
  }
}
