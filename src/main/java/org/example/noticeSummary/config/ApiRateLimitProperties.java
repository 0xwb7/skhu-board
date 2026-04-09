package org.example.noticeSummary.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.api.rate-limit")
public record ApiRateLimitProperties(
        boolean enabled,
        int windowSeconds,
        int maxRequests
) {
}
