package ai.concerto.event.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.businessmessages.v1.Businessmessages;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class ApplicationConfig {

  @SneakyThrows
  @Bean
  public String hostName() {
    return InetAddress.getLocalHost().getHostName();
  }

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.any()).build();
  }

  @Bean
  @Primary
  public ObjectMapper snakeCaseMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.NONE);
    mapper.setVisibility(PropertyAccessor.GETTER, Visibility.PROTECTED_AND_PUBLIC);
    mapper.setVisibility(PropertyAccessor.SETTER, Visibility.PROTECTED_AND_PUBLIC);
    mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    return mapper;
  }

  @Bean("camelCaseMapper")
  public ObjectMapper camelCaseMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.NONE);
    mapper.setVisibility(PropertyAccessor.GETTER, Visibility.PROTECTED_AND_PUBLIC);
    mapper.setVisibility(PropertyAccessor.SETTER, Visibility.PROTECTED_AND_PUBLIC);
    mapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
    mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    return mapper;
  }

  @Bean("objectMapper")
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.NONE);
    mapper.setVisibility(PropertyAccessor.GETTER, Visibility.PROTECTED_AND_PUBLIC);
    mapper.setVisibility(PropertyAccessor.SETTER, Visibility.PROTECTED_AND_PUBLIC);
    mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    return mapper;
  }

  @Bean("restTemplate")
  @Primary
  public RestTemplate restTemplate() {

    return new RestTemplate(getRequestFactory());
  }

  @Bean(name = "withOutEncoding")
  public RestTemplate restTemplateWithoutEncoding() {

    DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory();
    defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
    RestTemplate restTemplate = new RestTemplate(getRequestFactory());
    restTemplate.setUriTemplateHandler(defaultUriBuilderFactory);
    return restTemplate;
  }

  // Initializes credentials used by the Business Messages API.
  @Bean
  public Businessmessages.Builder getBusinessMessagesBuilder()
      throws IOException, GeneralSecurityException {
    Businessmessages.Builder builder = null;
    InputStream inputStream = getClass().getResourceAsStream("/google_business.json");
    GoogleCredential credential = GoogleCredential.fromStream(inputStream);
    credential =
        credential.createScoped(Arrays.asList("https://www.googleapis.com/auth/businessmessages"));
    credential.refreshToken();
    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    GsonFactory gsonFactory = GsonFactory.getDefaultInstance();
    // Create instance of the Business Messages API
    builder = new Businessmessages.Builder(httpTransport, gsonFactory, null)
        .setApplicationName("Concerto");
    // Set the API credentials and endpoint
    builder.setHttpRequestInitializer(credential);
    return builder;
  }

  private ClientHttpRequestFactory getRequestFactory() {

    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setMaxTotal(1000);
    connectionManager.setDefaultMaxPerRoute(500);
    RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(350000)
        .setSocketTimeout(350000).setConnectTimeout(350000).build();

    HttpClient httpClient = HttpClientBuilder.create().setConnectionManager(connectionManager)
        .setDefaultRequestConfig(requestConfig).setRedirectStrategy(new DefaultRedirectStrategy() {
          @Override
          public boolean isRedirected(HttpRequest request, HttpResponse response,
              HttpContext context) throws ProtocolException {
            // If redirect intercept intermediate response.
            if (super.isRedirected(request, response, context)) {
              request.removeHeaders("Authorization");
              return true;
            }
            return false;
          }
        }).setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
        .evictIdleConnections(300, TimeUnit.SECONDS).build();

    return new HttpComponentsClientHttpRequestFactory(httpClient);
  }

}
