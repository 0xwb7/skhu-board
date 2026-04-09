package org.example.noticeSummary.parser.school;

import org.example.noticeSummary.parser.model.ParsedNoticeDetail;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class SchoolNoticeDetailParserTest {

    private final SchoolNoticeDetailParser parser = new SchoolNoticeDetailParser();

    @Test
    void parsesSchoolNoticeFixture() throws IOException {
        ParsedNoticeDetail detail = parser.parse(
                "123",
                "https://www.skhu.ac.kr/bbs/skhu/26/123/artclView.do",
                Jsoup.parse(loadFixture("fixtures/school-notice-detail.html"))
        );

        assertThat(detail.title()).isEqualTo("2026학년도 장학금 신청 안내");
        assertThat(detail.date()).isEqualTo("2026.04.08");
        assertThat(detail.content()).isEqualTo("""
                장학금 신청 기간은 4월 20일까지입니다.
                제출 서류를 학과 사무실에 제출해주세요.
                """.trim());
    }

    private String loadFixture(String path) throws IOException {
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }
}
