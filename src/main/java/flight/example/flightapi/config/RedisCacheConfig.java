package flight.example.flightapi.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "prod") // Chỉ apply cho profile prod (Redis)
public class RedisCacheConfig {

   @Bean
   public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
      Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

      // Set TTL cho liveFlights: 10 phút (dữ liệu flight live thay đổi nhanh)
      cacheConfigurations.put("liveFlights", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10)) // TTL: 10 phút
            .disableCachingNullValues()); // Không cache giá trị null

      // Set TTL cho delayedFlights: 20 phút (dữ liệu delay ít thay đổi hơn)
      cacheConfigurations.put("delayedFlights", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(20))
            .disableCachingNullValues());

      // Có thể thêm các cache khác nếu cần, ví dụ: cache cho airline names nếu bạn
      // thêm @Cacheable ở nơi khác

      return RedisCacheManager.builder(redisConnectionFactory)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
   }
}