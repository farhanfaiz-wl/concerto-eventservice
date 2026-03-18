package ai.concerto.event.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;
import lombok.Getter;

@Getter
@ConfigurationProperties(prefix = "service")
public class ServiceDetails {

  private final Service falcon = new Service();
  private final Service analytics = new Service();
  private final Service seren = new Service();
  private final Service de = new Service();
  private final Service amazonBadging = new Service();
  private final Service nlu = new Service();
  private final Service emailClient = new Service();
  private final Service krishna = new Service();
  private final Service lobby = new Service();
  private final Service eventservice = new Service();
  private final Service telephony = new Service();
  private final Service base = new Service();
  private final Service ripples = new Service();

  @Data
  public static class Service {

    private String uri;
    private String appName;
    private String appKey;
    private String successUrl;
    // private Boolean enabled;
  }
}
