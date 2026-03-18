package ai.concerto.event.notification.render;

import ai.concerto.event.dto.Session;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.BotWhatsappCloudApiResponse;
import ai.concerto.event.exchange.BotWhatsappCloudApiResponse.WhatsappText;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WhatsAppNotificationCloudApiRendererImpl implements WhatsAppNotificationRenderer{
    @Override
    public Object setTimeOutResponse(ApplicationIntegration.ServiceProvider serviceProvider,
        Session session, String defaultSessionTimeoutPrompt,
        ApplicationIntegration.WhatsappIntegration whatsappIntegration) {
        BotWhatsappCloudApiResponse whatsappCloudApiResponse =
            new BotWhatsappCloudApiResponse();
        whatsappCloudApiResponse
            .setAccessToken(serviceProvider.getProviderDetails().getAccessToken());
        whatsappCloudApiResponse.setMessagingProduct("whatsapp");
        whatsappCloudApiResponse.setTo(session.getUserId());
        whatsappCloudApiResponse.setPhoneNumberId(session.getWhatsappPhoneNumberId());
        whatsappCloudApiResponse.setType("text");
        WhatsappText whatsappText = new WhatsappText();
        whatsappText.setBody(defaultSessionTimeoutPrompt);
        if (StringUtils.hasText(whatsappIntegration.getSessionTimeoutPrompt())) {
            whatsappText.setBody(whatsappIntegration.getSessionTimeoutPrompt());
        }
        whatsappCloudApiResponse.setText(whatsappText);
        whatsappCloudApiResponse.setTo(session.getUserId());
        return whatsappCloudApiResponse;
    }
}
