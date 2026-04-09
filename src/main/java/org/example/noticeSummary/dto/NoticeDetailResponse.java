package org.example.noticeSummary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NoticeDetailResponse {
    private String id;
    private String title;
    private String summary;
    private String organizedContent;
    private boolean aiGenerated;
    private String category;
    private String date;
    private String content;
    private String url;
    private String source;
}
