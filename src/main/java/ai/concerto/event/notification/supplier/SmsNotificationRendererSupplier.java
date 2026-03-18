package ai.concerto.event.notification.supplier;

import ai.concerto.event.enums.Vendor;
import ai.concerto.event.notification.render.SmsNotificationRendererImpl;
import ai.concerto.event.notification.render.SmsNotificationRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class SmsNotificationRendererSupplier {
	@Autowired
	private SmsNotificationRendererImpl smsNotificationRenderer;

	private SmsNotificationRendererSupplier() {
	}

	private final Supplier<SmsNotificationRenderer> TWILIO = () -> smsNotificationRenderer;
	private final Map<Vendor, Supplier<SmsNotificationRenderer>> builderMap = new EnumMap<>(Vendor.class);

	public void setBuilderMap() {
		builderMap.put(Vendor.TWILIO, TWILIO);
	}

	public SmsNotificationRenderer getSmsTimeOutRenderer(Vendor vendor) {
		setBuilderMap();
		return builderMap.get(vendor).get();
	}
}
