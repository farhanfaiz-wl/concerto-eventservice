package ai.concerto.event.config;

import ai.concerto.event.consumer.RedisStreamConsumer;
import ai.concerto.event.dto.UserRequestEvent;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroups;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.util.ObjectUtils;

@Configuration
public class RedisStreamConfig {

  @Value("${spring.redis.stream.consumer-group}")
  private String consumerGroup;

  @Value("${spring.redis.stream.log.key}")
  private String logStreamKey;

  @Value("${spring.redis.stream.internal.key}")
  private String internalStreamKey;

  @Value("${spring.redis.pubsub.channel.chatbot}")
  private String chatBotChannel;

  @Value("${spring.redis.stream.analytics.push_msgs.key}")
  private String pushMessageKey;

  @Bean
  public Subscription subscribeUserEvent(RedisConnectionFactory redisConnectionFactory,
      String hostName,
      StreamListener<String, ObjectRecord<String, UserRequestEvent>> userRequestStreamListener) {
    ensureConsumerStreamAndGroup(internalStreamKey, consumerGroup, redisConnectionFactory);
    StreamMessageListenerContainerOptions<String, ObjectRecord<String, UserRequestEvent>> options =
        StreamMessageListenerContainerOptions.builder().pollTimeout(Duration.ofSeconds(1))
            .targetType(UserRequestEvent.class).build();

    StreamMessageListenerContainer<String, ObjectRecord<String, UserRequestEvent>> listenerContainer =
        StreamMessageListenerContainer.create(redisConnectionFactory, options);

    StreamMessageListenerContainer.ConsumerStreamReadRequest<String> readOptions =
        StreamMessageListenerContainer.StreamReadRequest
            .builder(StreamOffset.create(internalStreamKey, ReadOffset.lastConsumed()))
            .cancelOnError(ex -> false).consumer(Consumer.from(consumerGroup, hostName)).build();

    Subscription subscription = listenerContainer.register(readOptions, userRequestStreamListener);
    listenerContainer.start();

    return subscription;
  }

  @Bean
  public Subscription subscription(RedisConnectionFactory redisConnectionFactory, String hostName,
      RedisStreamConsumer streamListener) {
    ensureConsumerStreamAndGroup(pushMessageKey, consumerGroup, redisConnectionFactory);

    StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
        StreamMessageListenerContainerOptions.builder().pollTimeout(Duration.ofSeconds(1)).build();

    StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer =
        StreamMessageListenerContainer.create(redisConnectionFactory, options);

    StreamMessageListenerContainer.ConsumerStreamReadRequest<String> readOptions =
        StreamMessageListenerContainer.StreamReadRequest
            .builder(StreamOffset.create(pushMessageKey, ReadOffset.lastConsumed()))
            .cancelOnError(ex -> false).consumer(Consumer.from(consumerGroup, hostName)).build();

    Subscription subscription = listenerContainer.register(readOptions, streamListener);
    listenerContainer.start();

    return subscription;
  }

  private void ensureConsumerStreamAndGroup(String consumerStreamKey, String consumerGroup,
      RedisConnectionFactory redisConnectionFactory) {
    AtomicBoolean isGroupAbsent = new AtomicBoolean(true);
    boolean mkStream = false;
    RedisConnection connection = redisConnectionFactory.getConnection();
    if (Boolean.FALSE.equals(connection.keyCommands().exists(consumerStreamKey.getBytes()))) {
      mkStream = true;
    } else {
      XInfoGroups xInfoGroups =
          connection.streamCommands().xInfoGroups(consumerStreamKey.getBytes());
      if (!ObjectUtils.isEmpty(xInfoGroups)) {
        xInfoGroups.forEach(xInfoGroup -> {
          if (xInfoGroup.groupName().equals(consumerGroup)) {
            isGroupAbsent.set(false);
          }
        });
      }
    }
    if (isGroupAbsent.get()) {
      connection.streamCommands().xGroupCreate(consumerStreamKey.getBytes(), consumerGroup,
          ReadOffset.latest(), mkStream);
    }
  }

  @Bean("chatBotTopic")
  public ChannelTopic chatBotTopic() {
    return new ChannelTopic(chatBotChannel);
  }
}
