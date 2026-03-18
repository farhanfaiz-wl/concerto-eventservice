package ai.concerto.event.notification.render;

import ai.concerto.event.dto.Session;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.BotWhatsappRouteResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WhatsAppRouteNotificationRendererImpl implements WhatsAppNotificationRenderer {
    @Override
    public Object setTimeOutResponse(ApplicationIntegration.ServiceProvider serviceProvider,
        Session session, String defaultSessionTimeoutPrompt,
        ApplicationIntegration.WhatsappIntegration whatsappIntegration) {
        BotWhatsappRouteResponse whatsappRouteResponse = new BotWhatsappRouteResponse();
        whatsappRouteResponse
            .setAuthId(serviceProvider.getProviderDetails().getAccountId());
        whatsappRouteResponse
            .setAuthToken(serviceProvider.getProviderDetails().getAuthToken());
        whatsappRouteResponse.setPhone(session.getUserId());
        whatsappRouteResponse.setText(defaultSessionTimeoutPrompt);
        if (StringUtils.hasText(whatsappIntegration.getSessionTimeoutPrompt())) {
            whatsappRouteResponse.setText(whatsappIntegration.getSessionTimeoutPrompt());
        }
        return whatsappRouteResponse;
    }
}
