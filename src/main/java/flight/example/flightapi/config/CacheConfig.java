package flight.example.flightapi.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@EnableCaching
@Profile("prod")
public class CacheConfig {

   @Bean
   public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
      RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10)) // Cache TTL 10 minutes
            .disableCachingNullValues();

      return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
   }
}