package ai.concerto.event.handler.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.exchange.MultiAppDetails;
import ai.concerto.event.service.MultiAppService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SmsTwilioRequestHandler extends SmsRequestHandler {

  @Autowired
  private ObjectMapper snakeCaseMapper;

  @Autowired
  private MultiAppService multiAppService;

  @SneakyThrows
  @Override
  public DERequest getDeRequest(String applicationId, Object request) {
    DERequest deRequest = getDeRequest(request);
    MultiAppDetails multiAppDetails =
        multiAppService.checkMultiApp(applicationId, deRequest, Channel.SMS);
    setIntegrationdetails(deRequest, applicationId, Vendor.TWILIO, multiAppDetails);
    return deRequest;
  }

}
