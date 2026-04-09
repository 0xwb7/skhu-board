package org.example.noticeSummary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NoticeResponse {
    private String id;
    private String title;
    private String summary;
    private String category;
    private String date;
    private String url;
    private String source;
}
