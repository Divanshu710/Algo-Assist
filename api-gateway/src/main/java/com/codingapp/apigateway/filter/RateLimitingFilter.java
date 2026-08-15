package com.codingapp.apigateway.filter;

import com.codingapp.apigateway.dto.ApiResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;

@Component
public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {

    @Autowired
    private LettuceBasedProxyManager<byte[]> proxyManager;

    @Autowired
    private ObjectMapper objectMapper;

    public RateLimitingFilter() {
        super(Config.class);
    }

    public static class Config {
        // Configuration properties can be mapped here later if needed
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 1. Extract the headers injected by JwtValidationFilter
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            String userTier = exchange.getRequest().getHeaders().getFirst("X-User-Tier");

            // Safety net: Reject if context headers are missing
            if (userId == null || userTier == null) {
                return onError(exchange, "Missing User Context for Rate Limiting", HttpStatus.UNAUTHORIZED);
            }

            // 2. Define bucket capacity dynamically based on user tier
            Supplier<BucketConfiguration> configSupplier = () -> getBucketConfiguration(userTier);

            // 3. Build or retrieve the rate limit bucket for this specific user from Redis
            io.github.bucket4j.Bucket bucket = proxyManager.builder()
                    .build(userId.getBytes(), configSupplier);

            // 4. Try to consume 1 token for this request
            if (bucket.tryConsume(1)) {
                // Token consumed successfully -> Forward to downstream service
                return chain.filter(exchange);
            } else {
                // Bucket empty -> Block the request with 429 Too Many Requests
                return onError(exchange,
                        "Rate limit exceeded. " + userTier + " tier limit reached. Please wait or upgrade.",
                        HttpStatus.TOO_MANY_REQUESTS);
            }
        };
    }

    private BucketConfiguration getBucketConfiguration(String tier) {
        // FREE: 2 requests/min | PREMIUM: 20 requests/min
        int capacity = "PREMIUM".equalsIgnoreCase(tier) ? 20 : 2;

        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(capacity, Duration.ofMinutes(1)) // Refills full capacity every minute
                .build();

        return BucketConfiguration.builder().addLimit(limit).build();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errorMessage, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            ApiResponse<Void> apiResponse = new ApiResponse<>(false, errorMessage, null, LocalDateTime.now());
            // Safe: Using Jackson 3 ObjectMapper
            byte[] bytes = objectMapper.writeValueAsBytes(apiResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JacksonException e) { // Safe: Catching Jackson 3 Exception
            return response.setComplete();
        }
    }
}