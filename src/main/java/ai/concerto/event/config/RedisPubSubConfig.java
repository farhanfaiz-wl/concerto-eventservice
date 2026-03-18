package ai.concerto.event.config;

import ai.concerto.event.subsciber.InboxNewConversationSubsciber;
import ai.concerto.event.subsciber.InboxNewMessageSubsciber;
import ai.concerto.event.subsciber.InboxV1NewMessageSubsciber;
import ai.concerto.event.subsciber.InboxV1NewTicketSubsciber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisPubSubConfig {

  @Value("${spring.redis.stream.analytics.inbox_v1_conversations.key}")
  private String inboxV1Conversations;

  @Value("${spring.redis.stream.analytics.inbox_v1_conversations_list.key}")
  private String inboxV1ConversationsList;

  @Value("${spring.redis.stream.analytics.inbox_conversations.key}")
  private String inboxConversations;

  @Value("${spring.redis.stream.analytics.inbox_conversations_list.key}")
  private String inboxConversationsList;


  @Bean
  RedisMessageListenerContainer subscribeInboxV1NewMessage(
      RedisConnectionFactory redisConnectionFactory,
      InboxV1NewMessageSubsciber inboxV1NewMessageSubsciber) {

    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory);
    container.addMessageListener(inboxV1NewMessageSubsciber,
        new PatternTopic(inboxV1Conversations));

    return container;
  }

  @Bean
  RedisMessageListenerContainer subscribeInboxV1NewTicket(
      RedisConnectionFactory redisConnectionFactory,
      InboxV1NewTicketSubsciber inboxV1NewTicketSubsciber) {

    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory);
    container.addMessageListener(inboxV1NewTicketSubsciber,
        new PatternTopic(inboxV1ConversationsList));

    return container;
  }

  @Bean
  RedisMessageListenerContainer subscribeInboxNewMessage(
      RedisConnectionFactory redisConnectionFactory,
      InboxNewMessageSubsciber inboxNewMessageSubsciber) {

    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory);
    container.addMessageListener(inboxNewMessageSubsciber, new PatternTopic(inboxConversations));

    return container;
  }

  @Bean
  RedisMessageListenerContainer subscribeInboxNewConversation(
      RedisConnectionFactory redisConnectionFactory,
      InboxNewConversationSubsciber inboxNewConversationSubsciber) {

    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory);
    container.addMessageListener(inboxNewConversationSubsciber,
        new PatternTopic(inboxConversationsList));

    return container;
  }

}
