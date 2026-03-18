package ai.concerto.event.notification.supplier;

import ai.concerto.event.notification.render.*;
import ai.concerto.event.enums.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class WhatsAppNotificationRendererSupplier {
	@Autowired
	private WhatsAppNotificationRendererImpl whatsAppNotificationRenderer;
	@Autowired
	private WhatsAppRouteNotificationRendererImpl whatsAppRouteNotificationRenderer;
	@Autowired
	private WhatsAppNotificationCloudApiRendererImpl whatsAppNotificationCloudApiRenderer;
	@Autowired
	private WhatsAppKaleyraNotificationRendererImpl whatsAppKaleyraNotificationRenderer;

	private WhatsAppNotificationRendererSupplier() {
	}

	private final Supplier<WhatsAppNotificationRenderer> TWILIO = () -> whatsAppNotificationRenderer;
	private final Supplier<WhatsAppNotificationRenderer> ROUTE = () -> whatsAppRouteNotificationRenderer;
	private final Supplier<WhatsAppNotificationRenderer> KALEYRA = () -> whatsAppKaleyraNotificationRenderer;
	private final Supplier<WhatsAppNotificationRenderer> VF = () -> whatsAppNotificationRenderer;
	private final Supplier<WhatsAppNotificationRenderer> CLOUD_API = () -> whatsAppNotificationCloudApiRenderer;

	private final Map<Vendor, Supplier<WhatsAppNotificationRenderer>> builderMap = new EnumMap<>(Vendor.class);

	public void setBuilderMap() {
		builderMap.put(Vendor.TWILIO, TWILIO);
		builderMap.put(Vendor.ROUTE, ROUTE);
		builderMap.put(Vendor.KALEYRA, KALEYRA);
		builderMap.put(Vendor.VF, VF);
		builderMap.put(Vendor.CLOUD_API, CLOUD_API);
	}

	public WhatsAppNotificationRenderer getWhatsAppTimeOutRenderer(Vendor vendor) {
		setBuilderMap();
		return builderMap.get(vendor).get();
	}
}
