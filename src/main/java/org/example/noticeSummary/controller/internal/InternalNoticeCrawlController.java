package org.example.noticeSummary.controller.internal;

import lombok.RequiredArgsConstructor;
import org.example.noticeSummary.crawler.lms.LmsAllNoticeListCrawler;
import org.example.noticeSummary.crawler.lms.LmsDepartmentNoticeListCrawler;
import org.example.noticeSummary.crawler.lms.LmsNoticeDetailCrawler;
import org.example.noticeSummary.crawler.school.SchoolNoticeDetailCrawler;
import org.example.noticeSummary.crawler.school.SchoolNoticeListCrawler;
import org.example.noticeSummary.dto.NoticeDetailResponse;
import org.example.noticeSummary.dto.NoticeResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crawl")
@ConditionalOnProperty(prefix = "app.crawl.internal-api", name = "enabled", havingValue = "true")
public class InternalNoticeCrawlController {

    private final SchoolNoticeListCrawler schoolNoticeListCrawler;
    private final SchoolNoticeDetailCrawler schoolNoticeDetailCrawler;
    private final LmsAllNoticeListCrawler lmsAllNoticeListCrawler;
    private final LmsNoticeDetailCrawler lmsNoticeDetailCrawler;
    private final LmsDepartmentNoticeListCrawler lmsDepartmentNoticeListCrawler;

    @GetMapping("/notices")
    public List<NoticeResponse> getNotices() {
        return schoolNoticeListCrawler.crawlNotices();
    }

    @GetMapping("/notices/{id}")
    public NoticeDetailResponse getNoticeDetail(@PathVariable String id) {
        return schoolNoticeDetailCrawler.crawlNoticeDetail(id);
    }

    @GetMapping("/lms/notices")
    public List<NoticeResponse> getLmsCommunityNotices() {
        return lmsAllNoticeListCrawler.crawlNotices();
    }

    @GetMapping("/lms/notices/detail")
    public NoticeDetailResponse getLmsNoticeDetail(@RequestParam String url, @RequestParam(required = false) String courseType) {
        return lmsNoticeDetailCrawler.crawlNoticeDetail(url, courseType);
    }

    @GetMapping("/lms/notices/it")
    public List<NoticeResponse> getItDepartmentNotices() {
        return lmsDepartmentNoticeListCrawler.crawlItDepartmentNotices();
    }

    @GetMapping("/lms/notices/software")
    public List<NoticeResponse> getSoftwareDepartmentNotices() {
        return lmsDepartmentNoticeListCrawler.crawlSoftwareDepartmentNotices();
    }
}
