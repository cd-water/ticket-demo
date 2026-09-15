package com.cdwater.cdticket.app.infrastructure.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 客户端（仅用于分布式锁等 Redisson 原生 API）。
 * 注意：使用核心 redisson 依赖而非 redisson-spring-boot-starter——
 * starter 的 RedissonConnectionFactory 会劫持 spring-data-redis 的连接工厂，
 * 与 Spring Data Redis 3.5 的 DefaultedRedisConnection 默认方法形成无限递归（StackOverflow）。
 */
@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(@Value("${spring.data.redis.host}") String host,
                                         @Value("${spring.data.redis.port}") int port,
                                         @Value("${spring.data.redis.password}") String password) {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setPassword(password);
        return Redisson.create(config);
    }
}
