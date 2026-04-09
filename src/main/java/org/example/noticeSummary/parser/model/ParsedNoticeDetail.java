package org.example.noticeSummary.parser.model;

public record ParsedNoticeDetail(
        String id,
        String title,
        String category,
        String date,
        String content,
        String url,
        String source
) {
}
