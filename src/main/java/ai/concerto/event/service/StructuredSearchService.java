package ai.concerto.event.service;

import ai.concerto.event.config.ServiceDetails;
import ai.concerto.event.dto.StructuredSearchRequest;
import ai.concerto.event.utils.RestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class StructuredSearchService {

  private static final String X_PROJECT_ID = "X-Project-Id";

  @Autowired
  private ServiceDetails serviceDetails;

  @Autowired
  private RestUtils restUtils;

  @Autowired
  private ObjectMapper mapper;

  public String fetchStructuredSearchFromSeren(String text, Integer size, String scrollTtl,
      StructuredSearchRequest request) {

    String uri = String.format("%s/projects/%s/search", serviceDetails.getSeren().getUri(),
        request.getProjectId());
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(uri);
    Map<String, String> params = new HashMap<>();
    if (StringUtils.hasLength(text)) {
      params.put("text", text);
    }
    if (!ObjectUtils.isEmpty(size)) {
      params.put("size", String.valueOf(size));
    }
    if (StringUtils.hasLength(scrollTtl)) {
      params.put("scroll_ttl", scrollTtl);
    }
    if (!CollectionUtils.isEmpty(params)) {
      params.forEach(uriComponentsBuilder::queryParam);
    }
    Properties headerProperties = new Properties();
    headerProperties.put("X-APIKEY", serviceDetails.getSeren().getAppKey());
    headerProperties.put(X_PROJECT_ID, request.getProjectId());

    try {
      log.debug("Fetching search result from seren for projectId {}", request.getProjectId());
      return restUtils.postRequest(uriComponentsBuilder.build().toUriString(),
          mapper.writeValueAsString(request.getParams()), headerProperties, String.class);
    } catch (Exception ex) {
      log.error("Error occurred while fetching search result from seren", ex);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error.");
    }
  }
}
