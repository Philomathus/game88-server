package tv.game88.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public Cache<String, ?> caffeineCache() {
        return Caffeine.newBuilder()
                // 设置最后一次写入或访问后经过固定时间过期
                .expireAfterWrite( Duration.ofSeconds( 2 ) )
                // 初始的缓存空间大小
                .initialCapacity( 100 )
                // 缓存的最大条数
                .maximumSize( Integer.MAX_VALUE )
                // 构建
                .build();
    }
}