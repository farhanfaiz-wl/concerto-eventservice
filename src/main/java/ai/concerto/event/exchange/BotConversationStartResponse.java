package ai.concerto.event.exchange;

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
public class BotConversationStartResponse {
	
	private String conversationId;
	private StatusResponse status;

	public static BotConversationStartResponse from(String conversationId) {
	  BotConversationStartResponse sessionResponse = new BotConversationStartResponse();
	  sessionResponse.setConversationId(conversationId);
	  sessionResponse.setStatus(new StatusResponse(201, "created", "Conversation has been created"));
	  return sessionResponse;
	}

}
