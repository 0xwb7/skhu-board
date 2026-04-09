package org.example.noticeSummary.mapper;

import org.example.noticeSummary.domain.Notice;
import org.example.noticeSummary.domain.NoticeCategory;
import org.example.noticeSummary.dto.NoticeDetailResponse;
import org.example.noticeSummary.dto.NoticeResponse;
import org.springframework.stereotype.Component;

@Component
public class NoticeResponseMapper {

    public NoticeResponse toNoticeResponse(Notice notice) {
        return new NoticeResponse(
                String.valueOf(notice.getId()),
                notice.getTitle(),
                safeText(notice.getSummary()),
                normalizeCategory(notice.getCategory()),
                safeText(notice.getDisplayDate()),
                notice.getOriginalUrl(),
                notice.getSource().name()
        );
    }

    public NoticeDetailResponse toNoticeDetailResponse(Notice notice) {
        return new NoticeDetailResponse(
                String.valueOf(notice.getId()),
                notice.getTitle(),
                safeText(notice.getSummary()),
                safeText(notice.getOrganizedContent()),
                notice.isAiGenerated(),
                normalizeCategory(notice.getCategory()),
                safeText(notice.getDisplayDate()),
                safeText(notice.getContent()),
                notice.getOriginalUrl(),
                notice.getSource().name()
        );
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }

    private String normalizeCategory(String category) {
        return NoticeCategory.fromLabel(category).label();
    }
}
