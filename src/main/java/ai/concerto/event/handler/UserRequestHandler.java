package ai.concerto.event.handler;

import org.springframework.stereotype.Service;
import ai.concerto.event.dto.DERequest;

@Service
public interface UserRequestHandler {

  DERequest getDeRequest(String applicationId, Object request);
}
