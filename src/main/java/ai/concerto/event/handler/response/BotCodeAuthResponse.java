package ai.concerto.event.handler.response;


import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BotCodeAuthResponse {

  private String code;
  private StatusResponse status;

  public BotCodeAuthResponse(String code) {
    this.code = code;
    if (!StringUtils.hasText(code))
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    this.status =
        new StatusResponse(HttpStatus.OK.value(), "success", "Successfully fetched auth code");
  }

  public BotCodeAuthResponse(StatusResponse status) {
    this.status = status;
  }
}
