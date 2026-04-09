package org.example.noticeSummary.parser.lms;

import org.example.noticeSummary.common.Constants;
import org.example.noticeSummary.parser.model.ParsedNoticeDetail;
import org.example.noticeSummary.util.CrawlerSupport;
import org.example.noticeSummary.util.LmsDateExtractor;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LmsNoticeDetailParserTest {

    private final CrawlerSupport crawlerSupport = mock(CrawlerSupport.class);
    private final LmsNoticeDetailParser parser = new LmsNoticeDetailParser(crawlerSupport, new LmsDateExtractor());

    @Test
    void parsesCommunityNoticeFixture() throws IOException {
        String url = "https://lms.skhu.ac.kr/ilos/community/notice_view_form.acl?ARTL_NUM=321";
        when(crawlerSupport.extractArticleId(url)).thenReturn("321");

        ParsedNoticeDetail detail = parser.parse(
                Jsoup.parse(loadFixture("fixtures/lms-community-notice-detail.html")),
                url,
                null
        );

        assertThat(detail.id()).isEqualTo("321");
        assertThat(detail.title()).isEqualTo("비교과 프로그램 신청 안내");
        assertThat(detail.date()).isEqualTo("2026.04.08 오후 3:15");
        assertThat(detail.content()).isEqualTo("""
                비교과 마일리지 프로그램 참여자를 모집합니다.
                신청은 LMS에서 진행합니다.
                """.trim());
    }

    @Test
    void parsesDepartmentNoticeFixture() throws IOException {
        String url = "https://lms.skhu.ac.kr/ilos/st/course/notice_view_form.acl?ARTL_NUM=777";
        when(crawlerSupport.extractArticleId(url)).thenReturn("777");

        ParsedNoticeDetail detail = parser.parse(
                Jsoup.parse(loadFixture("fixtures/lms-department-notice-detail.html")),
                url,
                Constants.COURSE_TYPE_IT
        );

        assertThat(detail.id()).isEqualTo("777");
        assertThat(detail.title()).isEqualTo("IT융합자율학부 세미나 안내");
        assertThat(detail.date()).isEqualTo("2026.04.07 오전 10:30");
        assertThat(detail.content()).isEqualTo("""
                세미나는 금요일 오후 2시에 진행됩니다.
                참석 대상: 재학생
                """.trim());
        assertThat(detail.source()).isEqualTo("LMS_IT");
    }

    private String loadFixture(String path) throws IOException {
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }
}
