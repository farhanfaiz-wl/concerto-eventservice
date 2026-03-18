package ai.concerto.event.notification.supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.handler.response.SmsTwilioResponseHandler;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SmsNotificationHandlerSupplierTest {
  @InjectMocks
  @Resource
  SmsNotificationHandlerSupplier smsNotificationHandlerSupplier;
  @Mock
  private SmsTwilioResponseHandler smsTwilioResponseHandler;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void getSmsResponseHandlerTest() {
    assertEquals(smsNotificationHandlerSupplier.getSmsResponseHandler(Vendor.TWILIO).getClass(),
        smsTwilioResponseHandler.getClass());
  }

}
