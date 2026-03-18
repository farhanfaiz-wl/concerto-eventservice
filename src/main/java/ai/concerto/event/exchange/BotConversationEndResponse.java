package ai.concerto.event.exchange;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import ai.concerto.event.handler.response.StatusResponse;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BotConversationEndResponse {

  private boolean deleted;
  private StatusResponse statusResponse;

  public BotConversationEndResponse(boolean deleted) throws ResponseStatusException {
    this.deleted = deleted;
    if (!deleted)
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    this.statusResponse = new StatusResponse(200, "success", "Conversation has been deleted");;
  }

}