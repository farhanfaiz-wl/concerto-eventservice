package ai.concerto.event.notification.supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.notification.render.WhatsAppKaleyraNotificationRendererImpl;
import ai.concerto.event.notification.render.WhatsAppNotificationRendererImpl;
import ai.concerto.event.notification.render.WhatsAppRouteNotificationRendererImpl;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class WhatsAppNotificationRendererSupplierTest {
  @InjectMocks
  @Resource
  WhatsAppNotificationRendererSupplier whatsAppNotificationRendererSupplier;
  @Mock
  private WhatsAppNotificationRendererImpl whatsAppTimeOutRendererImpl;
  @Mock
  private WhatsAppRouteNotificationRendererImpl whatsAppRouteTimeOutRendererImpl;
  @Mock
  private WhatsAppKaleyraNotificationRendererImpl whatsAppKaleyraNotificationRendererImpl;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void getWhatsAppTwilioRendererTest() {
    assertEquals(
        whatsAppNotificationRendererSupplier.getWhatsAppTimeOutRenderer(Vendor.TWILIO).getClass(),
        whatsAppTimeOutRendererImpl.getClass());
  }

  @Test
  void getWhatsAppRouteRendererTest() {
    assertEquals(
        whatsAppNotificationRendererSupplier.getWhatsAppTimeOutRenderer(Vendor.ROUTE).getClass(),
        whatsAppRouteTimeOutRendererImpl.getClass());
  }

  @Test
  void getWhatsAppVfRendererTest() {
    assertEquals(
        whatsAppNotificationRendererSupplier.getWhatsAppTimeOutRenderer(Vendor.VF).getClass(),
        whatsAppTimeOutRendererImpl.getClass());
  }

  @Test
  void getWhatsAppKalyeraRendererTest() {
    assertEquals(
        whatsAppNotificationRendererSupplier.getWhatsAppTimeOutRenderer(Vendor.KALEYRA).getClass(),
        whatsAppKaleyraNotificationRendererImpl.getClass());
  }


}
