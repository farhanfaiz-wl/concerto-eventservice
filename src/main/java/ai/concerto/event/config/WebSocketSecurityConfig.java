package ai.concerto.event.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

  @Override
  protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
    // enable authentication for inbox socket subscriptions
    messages.simpSubscribeDestMatchers("/user/queue/inbox_v1/**", "/user/queue/inbox/**")
        .authenticated().anyMessage().permitAll();
  }

  @Override
  protected boolean sameOriginDisabled() {
    return true;
  }
}
