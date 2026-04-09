package org.example.noticeSummary.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.noticeSummary.classification.RuleBasedNoticeCategoryClassifier;
import org.example.noticeSummary.domain.Notice;
import org.example.noticeSummary.domain.NoticeCategory;
import org.example.noticeSummary.domain.NoticeSource;
import org.example.noticeSummary.dto.NoticeDetailResponse;
import org.example.noticeSummary.repository.NoticeRepository;
import org.example.noticeSummary.support.NoticePublishedAtParser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeStoreService {

    private final NoticeRepository noticeRepository;
    private final RuleBasedNoticeCategoryClassifier ruleBasedNoticeCategoryClassifier;
    private final NoticePublishedAtParser noticePublishedAtParser;

    public boolean exists(NoticeSource source, String externalId) {
        return noticeRepository.findBySourceAndExternalId(source, externalId).isPresent();
    }

    public boolean save(NoticeSource source, String externalId, NoticeDetailResponse detail) {
        String content = safeText(detail.getContent());
        String displayDate = normalizeDisplayDate(detail.getDate());
        boolean aiGenerated = detail.isAiGenerated();
        String summary = aiGenerated ? normalizeOptionalText(detail.getSummary()) : null;
        String organizedContent = aiGenerated ? normalizeOptionalText(detail.getOrganizedContent()) : null;
        NoticeCategory category = resolveCategory(detail.getCategory(), detail.getTitle(), content);
        LocalDateTime now = LocalDateTime.now();

        Notice notice = Notice.builder()
                .source(source)
                .externalId(externalId)
                .title(detail.getTitle())
                .content(content)
                .summary(summary)
                .organizedContent(organizedContent)
                .category(category.label())
                .displayDate(displayDate)
                .publishedAt(noticePublishedAtParser.parsePublishedAt(displayDate))
                .originalUrl(detail.getUrl())
                .aiGenerated(aiGenerated && hasAiContent(summary, organizedContent))
                .collectedAt(now)
                .updatedAt(now)
                .build();

        try {
            noticeRepository.save(notice);
            return true;
        } catch (DataIntegrityViolationException ignored) {
            log.info("중복 공지 저장 생략. source={}, externalId={}", source, externalId);
            return false;
        }
    }

    private NoticeCategory resolveCategory(String categoryText, String title, String content) {
        NoticeCategory category = NoticeCategory.fromLabel(categoryText);
        if (category != NoticeCategory.ETC) {
            return category;
        }

        NoticeCategory classified = ruleBasedNoticeCategoryClassifier.classify(title, content);
        return classified == NoticeCategory.ETC ? category : classified;
    }

    private boolean hasAiContent(String summary, String organizedContent) {
        return summary != null && !summary.isBlank()
                && organizedContent != null && !organizedContent.isBlank();
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }

    private String normalizeOptionalText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return text.trim();
    }

    private String normalizeDisplayDate(String text) {
        String normalized = safeText(text).trim();
        if (normalized.length() > 255) {
            return normalized.substring(0, 255);
        }
        return normalized;
    }
}
