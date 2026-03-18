package ai.concerto.event.utils;

import java.util.Map;
import java.util.Properties;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.utils.Constants.HttpConstants;
import ai.concerto.event.utils.Constants.MdcConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RestUtils {

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  @Qualifier("objectMapper")
  private ObjectMapper objectMapper;

  public <T> T postRequest(String uri, String request, Properties headerProperties,
      Class<T> toValueType) throws Exception {
    return restRequest(uri, HttpMethod.POST, request, headerProperties, toValueType);
  }

  public <T> T postRequest(String uri, String request, MediaType contentType,
      Properties headerProperties, Class<T> toValueType) throws Exception {
    return restRequest(uri, HttpMethod.POST, request, contentType, headerProperties, toValueType);
  }

  public <T> T putRequest(String uri, String request, Properties headerProperties,
      Class<T> toValueType) throws Exception {
    return restRequest(uri, HttpMethod.PUT, request, headerProperties, toValueType);
  }

  public <T> T getRequest(String uri, Properties headerProperties, Class<T> toValueType)
      throws Exception {
    return restRequest(uri, HttpMethod.GET, null, headerProperties, toValueType);
  }

  public <T> T deleteRequest(String uri, Properties headerProperties, Class<T> toValueType)
      throws Exception {
    return restRequest(uri, HttpMethod.DELETE, null, headerProperties, toValueType);
  }

  private <T> T restRequest(String uri, HttpMethod method, String request,
      Properties headerProperties, Class<T> toValueType) throws Exception {
    return restRequest(uri, method, request, MediaType.APPLICATION_JSON, headerProperties,
        toValueType);
  }

  private <T> T restRequest(String uri, HttpMethod method, String request, MediaType contentType,
      Properties headerProperties, Class<T> toValueType) throws Exception {
    long startTime = System.currentTimeMillis();
    String response = restRequest(uri, method, request, contentType, headerProperties);
    long endTime = System.currentTimeMillis();
    log.debug("[{}] {}, request: {}, responseTime: {}", method.name().toLowerCase(), uri, request,
        endTime - startTime);
    try {
      if (toValueType.equals(String.class))
        return (T) response;

      return objectMapper.readValue(response, toValueType);
    } catch (JsonProcessingException e) {
      log.error("Unable to convert the response `{}` to {}", response, toValueType, e);
      throw e;
    }
  }

  private String restRequest(String uri, HttpMethod method, String request, MediaType contentType,
      Properties headerProperties) throws Exception {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(contentType);
    if (!ObjectUtils.isEmpty(MDC.get(MdcConstants.CORRELATION_ID)))
      headers.set(HttpConstants.HEADER_KEY_CORRELATION_ID, MDC.get(MdcConstants.CORRELATION_ID));

    if (headerProperties != null) {
      for (String headerName : headerProperties.stringPropertyNames()) {
        headers.set(headerName, headerProperties.getProperty(headerName));
      }
    }

    try {
      HttpEntity<?> entity = null;
      if (MediaType.APPLICATION_JSON.equals(contentType)) {
        entity = new HttpEntity<>(headers);
        if (StringUtils.hasText(request)) {
          entity = new HttpEntity<>(request, headers);
        }
      } else if (MediaType.APPLICATION_FORM_URLENCODED.equals(contentType)) {
        MultiValueMap<String, String> valueMap = new LinkedMultiValueMap<>();
        valueMap
            .setAll(objectMapper.readValue(request, new TypeReference<Map<String, String>>() {}));
        entity = new HttpEntity<>(valueMap, headers);
      }

      ResponseEntity<String> response = restTemplate.exchange(uri, method, entity, String.class);
      return response.getBody();
    } catch (Exception e) {
      log.error("Unable to make {} call for uri: {} with request body: {}", method.toString(), uri,
          request, e);
      throw e;
    }
  }
}
