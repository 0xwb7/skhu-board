package org.example.noticeSummary.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class InternalApiInterceptor implements HandlerInterceptor {

    private static final String INTERNAL_API_HEADER = "X-Internal-Api-Key";

    private final CrawlProperties crawlProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String configuredKey = crawlProperties.internalApi().key();
        String requestKey = request.getHeader(INTERNAL_API_HEADER);

        if (configuredKey != null && !configuredKey.isBlank() && configuredKey.equals(requestKey)) {
            return true;
        }

        response.sendError(HttpStatus.FORBIDDEN.value(), "내부 API 접근 권한이 없습니다.");
        return false;
    }
}
