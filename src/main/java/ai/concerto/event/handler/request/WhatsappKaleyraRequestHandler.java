package ai.concerto.event.handler.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.service.IntegrationService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WhatsappKaleyraRequestHandler extends WhatsappRequestHandler {

  @Autowired private ObjectMapper snakeCaseMapper;

  @Autowired private IntegrationService integrationService;

  @SneakyThrows
  @Override
  public DERequest getDeRequest(String applicationId, Object request) {
    DERequest deRequest = getDeRequest(applicationId, Vendor.KALEYRA);
    processWhatsappUserRequest(deRequest, request);

    return deRequest;
  }
  
}
