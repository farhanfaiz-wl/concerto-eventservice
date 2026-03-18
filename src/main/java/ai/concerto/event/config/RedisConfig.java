package ai.concerto.event.config;

import java.time.Duration;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.ClientOptions.DisconnectedBehavior;
import io.lettuce.core.RedisURI;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

@Configuration
@EnableCaching
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

  @Value("${spring.redis.url}")
  private String redisUrl;

  @Value("${spring.redis.cache.ttl}")
  private int cacheTtl;

  @Value("${spring.redis.ssl}")
  private boolean useSsl;

  @Bean(destroyMethod = "shutdown")
  public ClientResources clientResources() {
    return DefaultClientResources.create();
  }

  @Bean
  public RedisStandaloneConfiguration redisStandaloneConfiguration() {
    RedisURI redisURI = RedisURI.create(redisUrl);
    RedisStandaloneConfiguration redisConfig =
        new RedisStandaloneConfiguration(redisURI.getHost(), redisURI.getPort());
    if (StringUtils.hasText(redisURI.getUsername()))
      redisConfig.setUsername(redisURI.getUsername());
    if (!ObjectUtils.isEmpty(redisURI.getPassword()) && redisURI.getPassword().length > 0)
      redisConfig.setPassword(redisURI.getPassword());
    if (redisURI.getDatabase() >= 0)
      redisConfig.setDatabase(redisURI.getDatabase());

    return redisConfig;
  }

  @Bean
  public ClientOptions clientOptions() {
    return ClientOptions.builder().disconnectedBehavior(DisconnectedBehavior.REJECT_COMMANDS)
        .autoReconnect(true).build();
  }

  @Bean
  LettucePoolingClientConfiguration lettucePoolConfig(ClientOptions options,
      ClientResources resources) {
    LettucePoolingClientConfigurationBuilder configBuilder =
        LettucePoolingClientConfiguration.builder().poolConfig(new GenericObjectPoolConfig())
            .clientOptions(options).clientResources(resources);
    if (useSsl) {
      return configBuilder.useSsl().build();
    } else {
      return configBuilder.build();
    }

  }

  @Bean
  public RedisConnectionFactory redisConnectionFactory(
      RedisStandaloneConfiguration redisStandaloneConfiguration,
      LettucePoolingClientConfiguration lettucePoolConfig) {
    return new LettuceConnectionFactory(redisStandaloneConfiguration, lettucePoolConfig);
  }

  @Bean
  public RedisTemplate<Object, Object> redisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<Object, Object> template = new RedisTemplate<>();
    template.setDefaultSerializer(new StringRedisSerializer());
    template.setConnectionFactory(redisConnectionFactory);
    return template;
  }

  @Bean
  public RedisCacheConfiguration cacheConfiguration() {
    return RedisCacheConfiguration.defaultCacheConfig().prefixCacheNameWith("evs:")
        .entryTtl(Duration.ofMinutes(cacheTtl)).disableCachingNullValues().serializeValuesWith(
            SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
  }

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory,
      RedisCacheConfiguration redisCacheConfiguration) {
    return RedisCacheManagerBuilder.fromConnectionFactory(redisConnectionFactory)
        .cacheDefaults(redisCacheConfiguration).build();
  }
}
