package ai.concerto.event.notification.supplier;

import ai.concerto.event.enums.Vendor;
import ai.concerto.event.handler.BotResponseHandler;
import ai.concerto.event.handler.response.SmsTwilioResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class SmsNotificationHandlerSupplier {
	@Autowired
	private SmsTwilioResponseHandler smsTwilioResponseHandler;

	private SmsNotificationHandlerSupplier() {
	}

	private final Supplier<BotResponseHandler> TWILIO = () -> smsTwilioResponseHandler;
	private final Map<Vendor, Supplier<BotResponseHandler>> handlerMap = new EnumMap<>(Vendor.class);

	public void setHandlerMap() {
		handlerMap.put(Vendor.TWILIO, TWILIO);
	}

	public BotResponseHandler getSmsResponseHandler(Vendor vendor) {
		setHandlerMap();
		return handlerMap.get(vendor).get();
	}

}
