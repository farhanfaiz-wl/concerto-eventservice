package ai.concerto.event.handler.request;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.exchange.WhatsappUserRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WhatsappVFRequesthandler extends WhatsappRequestHandler {

  @Autowired
  private ObjectMapper mapper;


  @SneakyThrows
  @Override
  public DERequest getDeRequest(String applicationId, Object request) {
    DERequest deRequest = getDeRequest(applicationId, Vendor.VF);
    Map<String, String> vfEvent =
        mapper.convertValue(request, new TypeReference<Map<String, String>>() {});
    WhatsappUserRequest whatsappUserRequest = new WhatsappUserRequest(vfEvent.get("from"), null,
        null, vfEvent.get("text"), null, vfEvent.get("to"));
    processWhatsappUserRequest(deRequest, whatsappUserRequest);
    return deRequest;
  }
}
