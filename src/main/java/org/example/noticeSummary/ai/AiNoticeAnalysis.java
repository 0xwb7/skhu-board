package org.example.noticeSummary.ai;

import org.example.noticeSummary.domain.NoticeCategory;

public record AiNoticeAnalysis(
        boolean success,
        NoticeCategory category,
        String summary,
        String organizedContent
) {
}
