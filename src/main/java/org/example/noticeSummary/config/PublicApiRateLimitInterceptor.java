package org.example.noticeSummary.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class PublicApiRateLimitInterceptor implements HandlerInterceptor {

    private final ApiRateLimitProperties properties;
    private final Clock clock = Clock.systemUTC();
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!properties.enabled()) {
            return true;
        }

        long nowMillis = clock.millis();
        long windowMillis = properties.windowSeconds() * 1000L;
        String clientKey = resolveClientKey(request);

        WindowCounter counter = counters.compute(clientKey, (key, current) -> {
            if (current == null || current.windowStartedAt() + windowMillis <= nowMillis) {
                return new WindowCounter(nowMillis, new AtomicInteger(1));
            }

            current.requestCount().incrementAndGet();
            return current;
        });

        pruneExpiredCounters(nowMillis, windowMillis);

        if (counter.requestCount().get() <= properties.maxRequests()) {
            return true;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("""
                {"title":"Too Many Requests","detail":"요청이 너무 많습니다. 잠시 후 다시 시도해주세요."}
                """.trim());
        return false;
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private void pruneExpiredCounters(long nowMillis, long windowMillis) {
        if (counters.size() < 1_000) {
            return;
        }

        counters.entrySet().removeIf(entry -> entry.getValue().windowStartedAt() + windowMillis <= nowMillis);
    }

    private record WindowCounter(long windowStartedAt, AtomicInteger requestCount) {
    }
}
