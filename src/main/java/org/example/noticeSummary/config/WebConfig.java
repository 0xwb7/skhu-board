package org.example.noticeSummary.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final WebProperties webProperties;
    private final CrawlProperties crawlProperties;
    private final ApiRateLimitProperties apiRateLimitProperties;
    private final InternalApiInterceptor internalApiInterceptor;
    private final PublicApiRateLimitInterceptor publicApiRateLimitInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (webProperties.allowedOrigins().isEmpty()) {
            return;
        }

        registry.addMapping("/api/**")
                .allowedOriginPatterns(webProperties.allowedOrigins().toArray(String[]::new))
                .allowedMethods("GET")
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (!crawlProperties.internalApi().enabled()) {
            return;
        }

        registry.addInterceptor(internalApiInterceptor)
                .addPathPatterns("/api/crawl/**");

        if (apiRateLimitProperties.enabled()) {
            registry.addInterceptor(publicApiRateLimitInterceptor)
                    .addPathPatterns("/api/notices/**");
        }
    }
}
