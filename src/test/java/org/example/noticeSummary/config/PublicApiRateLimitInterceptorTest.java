package org.example.noticeSummary.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class PublicApiRateLimitInterceptorTest {

    @Test
    void blocksRequestsAfterConfiguredLimit() throws Exception {
        PublicApiRateLimitInterceptor interceptor =
                new PublicApiRateLimitInterceptor(new ApiRateLimitProperties(true, 60, 2));

        assertThat(allow(interceptor, "127.0.0.1")).isTrue();
        assertThat(allow(interceptor, "127.0.0.1")).isTrue();
        assertThat(allow(interceptor, "127.0.0.1")).isFalse();
    }

    private boolean allow(PublicApiRateLimitInterceptor interceptor, String remoteAddress) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/notices");
        request.setRemoteAddr(remoteAddress);
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean allowed = interceptor.preHandle(request, response, new Object());

        if (!allowed) {
            assertThat(response.getStatus()).isEqualTo(429);
        }

        return allowed;
    }
}
