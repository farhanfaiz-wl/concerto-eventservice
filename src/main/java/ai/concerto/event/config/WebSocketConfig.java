package ai.concerto.event.config;

import ai.concerto.event.exchange.InboxSocketUser;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.websocket.server.WsSci;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Autowired
  private JwtDecoder jwtDecoder;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {

    registry.setApplicationDestinationPrefixes("/socket");
    registry.enableSimpleBroker("/queue");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/event/socket/chatbot").setAllowedOriginPatterns("*");
    registry.addEndpoint("/event/socket/inbox_v1").setAllowedOriginPatterns("*");
    registry.addEndpoint("/event/socket/inbox").setAllowedOriginPatterns("*");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {

      @Override
      public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor stompHeaderAccessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (!ObjectUtils.isEmpty(stompHeaderAccessor)
            && StompCommand.CONNECT.equals(stompHeaderAccessor.getCommand())) {

          // get Bearer token from header if present
          List<String> authorization = stompHeaderAccessor.getNativeHeader("Authorization");
          String applicationId = Optional.ofNullable(stompHeaderAccessor.getNativeHeader("appId"))
              .map(appId -> appId.get(0)).orElse("");
          String userId = Optional.ofNullable(stompHeaderAccessor.getNativeHeader("userId"))
              .map(id -> id.get(0)).orElse("");
          // principal user is set here for chatbot
          stompHeaderAccessor.setUser(() -> applicationId + userId);

          Optional.ofNullable(authorization).ifPresent(auth -> {
            String accessToken = auth.get(0).split(" ")[1];
            jwtDecoder.decode(accessToken);
            InboxSocketUser inboxSocketUser = new InboxSocketUser(applicationId, true);
            /**
             * principal user is set here for inbox and should be set as authenticated as the inbox
             * subscriptions are authenticated in WebSocketSecurity
             */
            stompHeaderAccessor.setUser(inboxSocketUser);
          });
          log.debug("Socket connect event processed with user {}", stompHeaderAccessor.getUser());
        }
        return message;
      }
    });
  }

  @Bean
  public TomcatContextCustomizer tomcatContextCustomizer() {
    return context -> context.addServletContainerInitializer(new WsSci(), null);
  }

  @EventListener
  public void handleSessionConnectedEvent(SessionConnectedEvent event) {
    StompHeaderAccessor.wrap(event.getMessage());
  }

}
