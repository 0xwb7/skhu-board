package org.example.noticeSummary.persistence;

import org.example.noticeSummary.domain.Notice;
import org.example.noticeSummary.domain.NoticeSource;
import org.example.noticeSummary.dto.NoticeDetailResponse;
import org.example.noticeSummary.repository.NoticeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NoticeStoreServiceTest {

    @Autowired
    private NoticeStoreService noticeStoreService;

    @Autowired
    private NoticeRepository noticeRepository;

    @AfterEach
    void tearDown() {
        noticeRepository.deleteAll();
    }

    @Test
    void saveStoresAiFieldsOnlyWhenAiSucceeded() {
        NoticeDetailResponse detail = new NoticeDetailResponse(
                "external-1",
                "장학금 신청 안내",
                "AI 요약",
                "AI 정리",
                true,
                "장학",
                "2026.04.08",
                "본문",
                "https://example.com/notices/1",
                NoticeSource.SCHOOL.name()
        );

        boolean saved = noticeStoreService.save(NoticeSource.SCHOOL, "external-1", detail);

        assertThat(saved).isTrue();

        Notice notice = noticeRepository.findBySourceAndExternalId(NoticeSource.SCHOOL, "external-1").orElseThrow();
        assertThat(notice.isAiGenerated()).isTrue();
        assertThat(notice.getSummary()).isEqualTo("AI 요약");
        assertThat(notice.getOrganizedContent()).isEqualTo("AI 정리");
    }

    @Test
    void saveDropsFallbackAiFieldsWhenAiFailed() {
        NoticeDetailResponse detail = new NoticeDetailResponse(
                "external-2",
                "수강신청 안내",
                "AI 요약을 생성하지 못했습니다.",
                "AI가 상세 내용을 정리하지 못했습니다.",
                false,
                "기타",
                "2026.04.08",
                "수강신청 정정 기간 안내",
                "https://example.com/notices/2",
                NoticeSource.SCHOOL.name()
        );

        boolean saved = noticeStoreService.save(NoticeSource.SCHOOL, "external-2", detail);

        assertThat(saved).isTrue();

        Notice notice = noticeRepository.findBySourceAndExternalId(NoticeSource.SCHOOL, "external-2").orElseThrow();
        assertThat(notice.isAiGenerated()).isFalse();
        assertThat(notice.getSummary()).isNull();
        assertThat(notice.getOrganizedContent()).isNull();
        assertThat(notice.getCategory()).isEqualTo("수업");
    }
}
