package ai.concerto.event.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface Event {

  String data(ObjectMapper mapper);
}
