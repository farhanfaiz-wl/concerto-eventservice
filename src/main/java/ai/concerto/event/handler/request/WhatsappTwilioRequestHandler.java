package ai.concerto.event.handler.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Vendor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WhatsappTwilioRequestHandler extends WhatsappRequestHandler {


  @Autowired
  private ObjectMapper snakeCaseMapper;


  @SneakyThrows
  @Override
  public DERequest getDeRequest(String applicationId, Object request) {
    DERequest deRequest = getDeRequest(applicationId, Vendor.TWILIO);
    processWhatsappUserRequest(deRequest, request);
    return deRequest;
  }

}
