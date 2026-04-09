package org.example.noticeSummary.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.noticeSummary.common.Constants;
import org.example.noticeSummary.crawler.lms.LmsCrawlSession;
import org.example.noticeSummary.crawler.lms.LmsAllNoticeListCrawler;
import org.example.noticeSummary.crawler.lms.LmsDepartmentNoticeListCrawler;
import org.example.noticeSummary.crawler.lms.LmsNoticeDetailCrawler;
import org.example.noticeSummary.crawler.lms.LmsSessionFactory;
import org.example.noticeSummary.crawler.school.SchoolNoticeDetailCrawler;
import org.example.noticeSummary.crawler.school.SchoolNoticeListCrawler;
import org.example.noticeSummary.domain.NoticeSource;
import org.example.noticeSummary.dto.NoticeDetailResponse;
import org.example.noticeSummary.dto.NoticeResponse;
import org.example.noticeSummary.persistence.NoticeStoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeCollectionService {

    private final SchoolNoticeListCrawler schoolNoticeListCrawler;
    private final SchoolNoticeDetailCrawler schoolNoticeDetailCrawler;
    private final LmsAllNoticeListCrawler lmsAllNoticeListCrawler;
    private final LmsNoticeDetailCrawler lmsNoticeDetailCrawler;
    private final LmsDepartmentNoticeListCrawler lmsDepartmentNoticeListCrawler;
    private final LmsSessionFactory lmsSessionFactory;
    private final NoticeStoreService noticeStoreService;

    public void collectSchoolNotices() {
        collectNotices(
                schoolNoticeListCrawler.crawlNotices(),
                NoticeSource.SCHOOL,
                notice -> schoolNoticeDetailCrawler.crawlNoticeDetail(notice.getId())
        );
    }

    public void collectLmsAllNotices() {
        try (LmsCrawlSession session = lmsSessionFactory.createLoggedInSession()) {
            collectNotices(
                    lmsAllNoticeListCrawler.crawlNotices(session),
                    NoticeSource.LMS_ALL,
                    notice -> lmsNoticeDetailCrawler.crawlNoticeDetail(session, notice.getUrl(), null, false)
            );
        }
    }

    public void collectItDepartmentNotices() {
        collectDepartmentNotices(Constants.IT_COURSE, Constants.LMS_IT_NOTICE_LABEL, NoticeSource.LMS_IT, Constants.COURSE_TYPE_IT);
    }

    public void collectSoftwareDepartmentNotices() {
        collectDepartmentNotices(
                Constants.SOFTWARE_COURSE,
                Constants.LMS_SOFTWARE_NOTICE_LABEL,
                NoticeSource.LMS_SOFTWARE,
                Constants.COURSE_TYPE_SOFTWARE
        );
    }

    @Transactional
    public void collectNotices(List<NoticeResponse> notices, NoticeSource source,
                               Function<NoticeResponse, NoticeDetailResponse> detailFetcher) {
        for (NoticeResponse notice : notices) {
            if (noticeStoreService.exists(source, notice.getId())) {
                continue;
            }

            try {
                NoticeDetailResponse detail = detailFetcher.apply(notice);
                boolean saved = noticeStoreService.save(source, notice.getId(), detail);
                if (saved) {
                    log.info("공지 저장 완료. source={}, id={}, aiGenerated={}", source, notice.getId(), detail.isAiGenerated());
                }
            } catch (Exception e) {
                log.error("공지 수집 실패. source={}, id={}, url={}", source, notice.getId(), notice.getUrl(), e);
            }
        }
    }

    private void collectDepartmentNotices(String courseText, String category, NoticeSource source, String courseType) {
        try (LmsCrawlSession session = lmsSessionFactory.createLoggedInSession()) {
            collectNotices(
                    lmsDepartmentNoticeListCrawler.crawlDepartmentNotices(session, courseText, category, source),
                    source,
                    notice -> lmsNoticeDetailCrawler.crawlNoticeDetail(session, notice.getUrl(), courseType, false)
            );
        }
    }
}
