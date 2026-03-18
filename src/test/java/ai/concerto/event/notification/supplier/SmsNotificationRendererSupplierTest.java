package ai.concerto.event.notification.supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.notification.render.SmsNotificationRendererImpl;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SmsNotificationRendererSupplierTest {
  @InjectMocks
  @Resource
  SmsNotificationRendererSupplier smsNotificationRendererSupplier;
  @Mock
  private SmsNotificationRendererImpl smsTimeOutRendererImpl;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void getSmsTimeOutRendererTest() {
    assertEquals(smsNotificationRendererSupplier.getSmsTimeOutRenderer(Vendor.TWILIO).getClass(),
        smsTimeOutRendererImpl.getClass());
  }

}
