package com.codingapp.apigateway.config;

import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;

    @Value("${spring.data.redis.ssl.enabled}")
    private boolean sslEnabled;

    @Bean
    public RedisClient redisClient(){

        RedisURI.Builder builder = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withSsl(sslEnabled);

        if(password != null && !password.trim().isEmpty()){
            builder.withPassword(password.toCharArray());
        }

        return RedisClient.create(builder.build());
    }

    @Bean
    public LettuceBasedProxyManager<byte[]> proxyManager(RedisClient redisClient) {
        return LettuceBasedProxyManager.builderFor(redisClient)
                .build();
    }

}
