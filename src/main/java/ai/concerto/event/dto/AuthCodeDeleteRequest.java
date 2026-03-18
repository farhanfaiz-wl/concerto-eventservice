package ai.concerto.event.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthCodeDeleteRequest {

	private String applicationId;
	private String userId;
}
