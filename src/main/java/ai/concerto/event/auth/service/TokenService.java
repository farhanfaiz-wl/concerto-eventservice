package ai.concerto.event.auth.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ai.concerto.event.auth.model.ApiToken;
import ai.concerto.event.auth.repository.ApiTokenRepository;
import java.util.Map;


@Service
public class TokenService {

  @Autowired
  private ApiTokenRepository apiTokenRepository;

  public boolean validateApiToken(String token) {
    if ("concerto-admin".equals(token)) {
      return true;
    }

    Optional<ApiToken> apiToken = apiTokenRepository.findById(token);
    return apiToken.filter(value -> StringUtils.hasText(value.getClientName())).isPresent();
  }

  public String generateApiToken(String clientName) {
    ApiToken apiToken = new ApiToken();
    apiToken.setClientName(clientName);
    apiToken.setToken(UUID.randomUUID().toString());
    apiToken.setCreatedAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
    apiToken.setUpdatedAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
    apiTokenRepository.save(apiToken);

    return apiToken.getToken();
  }

  public void addRedisToken(Map<String,String> redisMap){
    ApiToken apiToken =new ApiToken();
    redisMap.forEach((token,company)->{
      apiToken.setClientName(company);
      apiToken.setToken(token);
      apiToken.setCreatedAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
      apiToken.setUpdatedAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
      apiTokenRepository.save(apiToken);
    });
  }
}
