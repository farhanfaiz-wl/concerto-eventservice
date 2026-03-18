package ai.concerto.event.render;

import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;

public interface ResponseRenderer {

  Object render(Object userRequest, DERequest deRequest, DEBotResponse deResponse);
}
