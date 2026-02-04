package com.hotel.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientId = getClientId(exchange);

        RateLimitInfo rateLimitInfo = rateLimitMap.computeIfAbsent(
                clientId,
                k -> new RateLimitInfo()
        );

        if (rateLimitInfo.isExpired()) {
            rateLimitInfo.reset();
        }

        if (rateLimitInfo.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().add(
                    "X-RateLimit-Retry-After-Seconds",
                    String.valueOf(rateLimitInfo.getSecondsUntilReset())
            );
            return exchange.getResponse().setComplete();
        }

        exchange.getResponse().getHeaders().add(
                "X-RateLimit-Limit",
                String.valueOf(MAX_REQUESTS_PER_MINUTE)
        );
        exchange.getResponse().getHeaders().add(
                "X-RateLimit-Remaining",
                String.valueOf(MAX_REQUESTS_PER_MINUTE - rateLimitInfo.getCount())
        );

        return chain.filter(exchange);
    }

    private String getClientId(ServerWebExchange exchange) {
        List<String> userIdHeaders = exchange.getRequest().getHeaders().get("X-User-Id");

        if (userIdHeaders != null && !userIdHeaders.isEmpty()) {
            return "user:" + userIdHeaders.getFirst();
        }

        String clientIp = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        return "ip:" + clientIp;
    }

    @Override
    public int getOrder() {
        return -90;
    }

    private static class RateLimitInfo {
        private final AtomicInteger count = new AtomicInteger(0);
        private long windowStart = System.currentTimeMillis();

        public int incrementAndGet() {
            return count.incrementAndGet();
        }

        public int getCount() {
            return count.get();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - windowStart > WINDOW_DURATION.toMillis();
        }

        public void reset() {
            count.set(0);
            windowStart = System.currentTimeMillis();
        }

        public long getSecondsUntilReset() {
            long elapsed = System.currentTimeMillis() - windowStart;
            long remaining = WINDOW_DURATION.toMillis() - elapsed;
            return Math.max(0, remaining / 1000);
        }
    }
}
