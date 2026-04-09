package org.example.noticeSummary.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.crawl")
public record CrawlProperties(
        boolean scheduleEnabled,
        InternalApi internalApi
) {

    public record InternalApi(
            boolean enabled,
            String key
    ) {
    }
}
