package ai.concerto.event.notification.render;

import ai.concerto.event.dto.Session;
import ai.concerto.event.exchange.ApplicationIntegration;
import ai.concerto.event.exchange.BotFacebookResponse;
import org.springframework.util.StringUtils;

public class FaceBookNotificationRendererImpl implements FaceBookNotificationRenderer {
	@Override
	public BotFacebookResponse setTimeOutResponse(String projectId, Session session, String defaultSessionTimeoutPrompt,
			ApplicationIntegration.FacebookIntegration facebookIntegration) {
		BotFacebookResponse facebookResponse = new BotFacebookResponse();
		facebookResponse.setProjectId(projectId);
		facebookResponse.getMessage().setText(defaultSessionTimeoutPrompt);
		if (StringUtils.hasText(facebookIntegration.getSessionTimeoutPrompt())) {
			facebookResponse.getMessage().setText(facebookIntegration.getSessionTimeoutPrompt());
		}
		facebookResponse.getMessage().setMetadata("DEVELOPER_DEFINED_METADATA");
		facebookResponse.getRecipient().setId(session.getUserId());
		return facebookResponse;
	}
}
