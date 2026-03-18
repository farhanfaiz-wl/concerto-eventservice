package ai.concerto.event.handler.request;

import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Vendor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WhatsappRouteRequestHandler extends WhatsappRequestHandler {

  @Autowired private ObjectMapper snakeCaseMapper;

  @SneakyThrows
  @Override
  public DERequest getDeRequest(String applicationId, Object request) {
    DERequest deRequest = getDeRequest(applicationId, Vendor.ROUTE);
    processWhatsappUserRequest(deRequest, request);

    return deRequest;
  }
}
