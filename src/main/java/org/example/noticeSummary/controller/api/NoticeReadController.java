package org.example.noticeSummary.controller.api;

import lombok.RequiredArgsConstructor;
import org.example.noticeSummary.application.NoticeReadService;
import org.example.noticeSummary.dto.NoticeDetailResponse;
import org.example.noticeSummary.dto.NoticeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeReadController {

    private final NoticeReadService noticeReadService;

    @GetMapping
    public List<NoticeResponse> getNotices() {
        return noticeReadService.getRecentNotices();
    }

    @GetMapping("/{id}")
    public NoticeDetailResponse getNoticeDetail(@PathVariable Long id) {
        return noticeReadService.getNoticeDetail(id);
    }
}
