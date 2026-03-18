package ai.concerto.event.notification.render;

import ai.concerto.event.dto.Session;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.BotSmsResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SmsNotificationRendererImpl implements SmsNotificationRenderer {
    @Override
    public Object setTimeOutResponse(Session session, String defaultSessionTimeoutPrompt,
        ApplicationIntegration.SmsIntegration smsIntegration) {
        BotSmsResponse smsResponse = new BotSmsResponse();
        ApplicationIntegration.ServiceProvider
            serviceProvider = smsIntegration.getServiceProvidersList().get(0);
        smsResponse.setAuthId(serviceProvider.getProviderDetails().getAccountId());
        smsResponse.setAuthToken(serviceProvider.getProviderDetails().getAuthToken());
        smsResponse.setTo(session.getUserId());
        smsResponse.setFrom(smsIntegration.getPhoneNumber());
        smsResponse.setMessage(defaultSessionTimeoutPrompt);
        if (StringUtils.hasText(smsIntegration.getSessionTimeoutPrompt())) {
            smsResponse.setMessage(smsIntegration.getSessionTimeoutPrompt());
        }
        return smsResponse;
    }
}
