package ai.concerto.event.notification.supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.handler.response.WhatsappKaleyraResponseHandler;
import ai.concerto.event.handler.response.WhatsappRouteResponseHandler;
import ai.concerto.event.handler.response.WhatsappTwilioResponseHandler;
import ai.concerto.event.handler.response.WhatsappVFResponseHandler;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class WhatsAppNotificationHandlerSupplierTest {
  @InjectMocks
  @Resource
  WhatsAppNotificationHandlerSupplier whatsAppNotificationHandlerSupplier;

  @Mock
  private WhatsappTwilioResponseHandler whatsappTwilioResponseHandler;
  @Mock
  private WhatsappKaleyraResponseHandler whatsappKaleyraResponseHandler;
  @Mock
  private WhatsappRouteResponseHandler whatsappRouteResponseHandler;
  @Mock
  private WhatsappVFResponseHandler whatsappVFResponseHandler;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void getWhatsAppBotResponseHandlerTwilioTest() {
    assertEquals(
        whatsAppNotificationHandlerSupplier.getWhatsAppBotResponseHandler(Vendor.TWILIO).getClass(),
        whatsappTwilioResponseHandler.getClass());
  }

  @Test
  void getWhatsAppBotResponseHandlerTestRoute() {
    assertEquals(
        whatsAppNotificationHandlerSupplier.getWhatsAppBotResponseHandler(Vendor.ROUTE).getClass(),
        whatsappRouteResponseHandler.getClass());
  }

  @Test
  void getWhatsAppBotRouteResponseHandlerVf() {
    assertEquals(
        whatsAppNotificationHandlerSupplier.getWhatsAppBotResponseHandler(Vendor.VF).getClass(),
        whatsappVFResponseHandler.getClass());
  }

  @Test
  void getWhatsAppBotRouteResponseHandlerKalyera() {
    assertEquals(whatsAppNotificationHandlerSupplier.getWhatsAppBotResponseHandler(Vendor.KALEYRA)
        .getClass(), whatsappKaleyraResponseHandler.getClass());
  }
}
