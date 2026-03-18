package ai.concerto.event.notification.render;

import ai.concerto.event.dto.Session;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.BotWhatsappResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WhatsAppKaleyraNotificationRendererImpl implements WhatsAppNotificationRenderer {
    @Override
    public Object setTimeOutResponse(ApplicationIntegration.ServiceProvider serviceProvider,
                                     Session session, String defaultSessionTimeoutPrompt,
                                     ApplicationIntegration.WhatsappIntegration whatsappIntegration) {

        BotWhatsappResponse whatsappResponse = new BotWhatsappResponse();
        whatsappResponse.setAuthId(serviceProvider.getProviderDetails().getAccountId());
        whatsappResponse.setAuthToken(serviceProvider.getProviderDetails().getAuthToken());
        whatsappResponse.setTo(session.getUserId());
        whatsappResponse.setFrom(whatsappIntegration.getPhoneNumber());
        whatsappResponse.setType("text");
        whatsappResponse.setChannel("whatsapp");
        whatsappResponse.setBody(defaultSessionTimeoutPrompt);
        if (StringUtils.hasText(whatsappIntegration.getSessionTimeoutPrompt())) {
            whatsappResponse.setBody(whatsappIntegration.getSessionTimeoutPrompt());
        }
        return whatsappResponse;
    }
}
