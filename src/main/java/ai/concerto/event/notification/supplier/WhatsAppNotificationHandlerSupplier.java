package ai.concerto.event.notification.supplier;

import ai.concerto.event.enums.Vendor;
import ai.concerto.event.handler.BotResponseHandler;
import ai.concerto.event.handler.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class WhatsAppNotificationHandlerSupplier {
	@Autowired
	private WhatsappTwilioResponseHandler whatsappTwilioResponseHandler;
	@Autowired
	private WhatsappKaleyraResponseHandler whatsappKaleyraResponseHandler;
	@Autowired
	private WhatsappRouteResponseHandler whatsappRouteResponseHandler;
	@Autowired
	private WhatsappVFResponseHandler whatsappVFResponseHandler;
	@Autowired
	private WhatsappCloudApiResponseHandler whatsappCloudApiResponseHandler;

	private WhatsAppNotificationHandlerSupplier() {
	}

	private final Supplier<BotResponseHandler> TWILIO = () -> whatsappTwilioResponseHandler;
	private final Supplier<BotResponseHandler> ROUTE = () -> whatsappRouteResponseHandler;
	private final Supplier<BotResponseHandler> KALEYRA = () -> whatsappKaleyraResponseHandler;
	private final Supplier<BotResponseHandler> VF = () -> whatsappVFResponseHandler;
	private final Supplier<BotResponseHandler> CLOUD_API = () -> whatsappCloudApiResponseHandler;

	private final Map<Vendor, Supplier<BotResponseHandler>> handlerMap = new EnumMap<>(Vendor.class);

	public void setHandlerMap() {
		handlerMap.put(Vendor.TWILIO, TWILIO);
		handlerMap.put(Vendor.ROUTE, ROUTE);
		handlerMap.put(Vendor.KALEYRA, KALEYRA);
		handlerMap.put(Vendor.VF, VF);
		handlerMap.put(Vendor.CLOUD_API, CLOUD_API);
	}

	public BotResponseHandler getWhatsAppBotResponseHandler(Vendor vendor) {
		setHandlerMap();
		return handlerMap.get(vendor).get();
	}
}
