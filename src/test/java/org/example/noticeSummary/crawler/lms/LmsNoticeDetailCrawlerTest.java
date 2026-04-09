package org.example.noticeSummary.crawler.lms;

import org.example.noticeSummary.ai.NoticeDetailAiService;
import org.example.noticeSummary.parser.lms.LmsNoticeDetailParser;
import org.example.noticeSummary.util.CrawlerSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class LmsNoticeDetailCrawlerTest {

    @Test
    void rejectsUntrustedDetailUrlBeforeDriverAccess() {
        LmsNoticeDetailCrawler crawler = new LmsNoticeDetailCrawler(
                mock(LmsSessionFactory.class),
                mock(LmsNoticeDetailParser.class),
                mock(NoticeDetailAiService.class),
                mock(CrawlerSupport.class)
        );

        assertThatThrownBy(() -> crawler.crawlNoticeDetail("https://evil.example.com/phishing", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않은 LMS 상세 URL");
    }
}
