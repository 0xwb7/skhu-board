package org.example.noticeSummary.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.web")
public record WebProperties(
        List<String> allowedOrigins
) {

    public WebProperties {
        allowedOrigins = allowedOrigins == null ? new ArrayList<>() : List.copyOf(allowedOrigins);
    }
}
