package cn.iocoder.yudao.module.system.framework.justauth.config;

import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import com.xkcoding.justauth.support.cache.RedisStateCache;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.cache.AuthDefaultStateCache;
import me.zhyd.oauth.cache.AuthStateCache;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * <p>
 * JustAuth 缓存装配类
 * </p>
 */
@Slf4j
@Configuration
public class JustAuthStateCacheConfiguration {

    /**
     * Redis 缓存配置
     */
    @Configuration
    @ConditionalOnClass(RedisTemplate.class)
    @ConditionalOnProperty(name = "justauth.cache.type", havingValue = "redis", matchIfMissing = true)
    @AutoConfigureBefore(RedissonAutoConfiguration.class)
    public static class RedisCacheConfiguration {

        static {
            log.debug("JustAuth 使用 Redis 缓存存储 state 数据");
        }

        @Bean(name = "justAuthRedisCacheTemplate")
        public RedisTemplate<String, String> justAuthRedisCacheTemplate(RedisConnectionFactory redisConnectionFactory) {
            RedisTemplate<String, String> template = new RedisTemplate<>();
            template.setKeySerializer(new StringRedisSerializer());
            template.setConnectionFactory(redisConnectionFactory);
            return template;
        }

        @Bean
        @Primary
        @ConditionalOnMissingBean(AuthStateCache.class)
        public AuthStateCache authStateCache(
                @Qualifier("justAuthRedisCacheTemplate") RedisTemplate<String, String> redisTemplate,
                JustAuthProperties justAuthProperties) {
            return new RedisStateCache(redisTemplate, justAuthProperties.getCache());
        }
    }

    /**
     * 默认缓存配置（内存缓存）
     */
    @Configuration
    @ConditionalOnProperty(name = "justauth.cache.type", havingValue = "default", matchIfMissing = false)
    public static class DefaultCacheConfiguration {

        static {
            log.debug("JustAuth 使用默认缓存存储 state 数据");
        }

        @Bean
        @ConditionalOnMissingBean(AuthStateCache.class)
        public AuthStateCache authStateCache() {
            return AuthDefaultStateCache.INSTANCE;
        }
    }

    /**
     * 自定义缓存配置
     * 用户需要自行实现 AuthStateCache 接口并注入到 Spring 容器
     */
    @Configuration
    @ConditionalOnProperty(name = "justauth.cache.type", havingValue = "custom")
    public static class CustomCacheConfiguration {

        static {
            log.debug("JustAuth 使用自定义缓存存储 state 数据");
        }

        @Bean
        @ConditionalOnMissingBean(AuthStateCache.class)
        public AuthStateCache authStateCache() {
            log.error("检测到 justauth.cache.type=custom，但未找到 AuthStateCache 的实现 Bean，请自行实现 me.zhyd.oauth.cache.AuthStateCache 并注入到 Spring 容器");
            throw new IllegalStateException("请自行实现 me.zhyd.oauth.cache.AuthStateCache 并注入到 Spring 容器");
        }
    }
}