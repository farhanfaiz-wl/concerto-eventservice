package ai.concerto.event.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import lombok.Getter;
import org.apache.commons.codec.binary.Base64;

@Getter
public class JWT {

  private static ObjectMapper mapper;
  private Map<String, Object> jwtHeader;
  private Map<String, Object> jwtBody;

  private JWT(Map<String, Object> jwtHeader, Map<String, Object> jwtBody) {
    this.jwtHeader = jwtHeader;
    this.jwtBody = jwtBody;
    setObjectMapper();
  }

  private void setObjectMapper() {
    mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.NONE);
    mapper.setVisibility(PropertyAccessor.GETTER, Visibility.PROTECTED_AND_PUBLIC);
    mapper.setVisibility(PropertyAccessor.SETTER, Visibility.PROTECTED_AND_PUBLIC);
    mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static JWT parse(String jwtToken) throws IOException {
    // TODO: (CP2017-668) need to add the validation for jwt signature. Ideally Reverse Proxy should
    // do it.
    String[] splitStringArray = jwtToken.split("\\.");
    String base64EncodedHeader = splitStringArray[0];
    String base64EncodedBody = splitStringArray[1];

    Base64 base64Url = new Base64(true);
    Map<String, Object> jwtHeader =
        mapper.readValue(
            new String(base64Url.decode(base64EncodedHeader)),
            new TypeReference<Map<String, Object>>() {});
    Map<String, Object> jwtBody =
        mapper.readValue(
            new String(base64Url.decode(base64EncodedBody)),
            new TypeReference<Map<String, Object>>() {});
    return new JWT(jwtHeader, jwtBody);
  }
}
