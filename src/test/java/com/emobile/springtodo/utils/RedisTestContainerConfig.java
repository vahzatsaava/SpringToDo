package com.emobile.springtodo.utils;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration
public class RedisTestContainerConfig {

    static final GenericContainer<?> redisContainer;

    static {
        redisContainer = new GenericContainer<>("redis:7.0.11-alpine")
                .withExposedPorts(6379);
        redisContainer.start();
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisContainer.getHost());
        redisConfig.setPort(redisContainer.getMappedPort(6379));
        return new LettuceConnectionFactory(redisConfig);
    }
}
