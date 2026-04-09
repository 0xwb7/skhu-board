package org.example.noticeSummary.ai;

public record AiNoticeAnalysisPayload(
        String category,
        String summary,
        String organizedContent
) {
}
