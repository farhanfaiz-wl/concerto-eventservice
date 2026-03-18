package ai.concerto.event.auth.model;

import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import lombok.Data;

@Data
@RedisHash("api_token")
public class ApiToken implements Serializable {

  @Id
  private String token;
  private String clientName;
  private Long createdAt;
  private Long updatedAt;
}
