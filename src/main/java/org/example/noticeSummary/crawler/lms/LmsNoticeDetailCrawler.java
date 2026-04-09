package org.example.noticeSummary.crawler.lms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.noticeSummary.ai.NoticeDetailAiService;
import org.example.noticeSummary.common.Constants;
import org.example.noticeSummary.dto.NoticeDetailResponse;
import org.example.noticeSummary.parser.lms.LmsNoticeDetailParser;
import org.example.noticeSummary.parser.model.ParsedNoticeDetail;
import org.example.noticeSummary.util.CrawlerSupport;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Service;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class LmsNoticeDetailCrawler {

    private final LmsSessionFactory lmsSessionFactory;
    private final LmsNoticeDetailParser lmsNoticeDetailParser;
    private final NoticeDetailAiService noticeDetailAiService;
    private final CrawlerSupport crawlerSupport;

    public NoticeDetailResponse crawlNoticeDetail(String detailUrl, String courseType) {
        validateDetailUrl(detailUrl);

        try (LmsCrawlSession session = lmsSessionFactory.createLoggedInSession()) {
            return crawlNoticeDetail(session, detailUrl, courseType, true);
        }
    }

    public NoticeDetailResponse crawlNoticeDetail(LmsCrawlSession session, String detailUrl, String courseType, boolean prepareCourseContext) {
        try {
            validateDetailUrl(detailUrl);

            if (prepareCourseContext && isDepartmentNotice(detailUrl)) {
                enterDepartmentIfNeeded(session, courseType);
            }

            session.driver().get(detailUrl);
            dismissAlertIfPresent(session);
            waitUntilDetailLoaded(session, detailUrl);

            String html = session.driver().getPageSource();
            Document doc = Jsoup.parse(html);
            ParsedNoticeDetail parsed = lmsNoticeDetailParser.parse(doc, detailUrl, courseType);
            return noticeDetailAiService.createNoticeDetailResponse(parsed);

        } catch (UnhandledAlertException e) {
            log.error("LMS 상세 공지 접근 중 alert 발생. url={}", detailUrl, e);
            throw new RuntimeException("LMS 상세 공지 접근 실패", e);
        } catch (Exception e) {
            log.error("LMS 상세 공지 크롤링 실패. url={}, courseType={}", detailUrl, courseType, e);
            throw new RuntimeException("LMS 상세 공지 크롤링 실패", e);
        }
    }

    private void validateDetailUrl(String detailUrl) {
        try {
            URI uri = URI.create(detailUrl);
            String host = uri.getHost();
            String path = uri.getPath();

            boolean trustedHost = "lms.skhu.ac.kr".equalsIgnoreCase(host);
            boolean trustedPath = path != null && (
                    path.startsWith("/ilos/community/notice_view_form.acl")
                            || path.startsWith("/ilos/st/course/notice_view_form.acl")
            );

            if (!trustedHost || !trustedPath) {
                throw new IllegalArgumentException("허용되지 않은 LMS 상세 URL 입니다.");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 LMS 상세 URL 입니다.", e);
        }
    }

    private boolean isDepartmentNotice(String detailUrl) {
        return lmsNoticeDetailParser.isDepartmentNotice(detailUrl);
    }

    private boolean isCommunityNotice(String detailUrl) {
        return lmsNoticeDetailParser.isCommunityNotice(detailUrl);
    }

    private void enterDepartmentIfNeeded(LmsCrawlSession session, String courseType) {
        if (Constants.COURSE_TYPE_IT.equalsIgnoreCase(courseType)) {
            crawlerSupport.enterDepartmentCourse(session.driver(), session.driverWait(), Constants.IT_COURSE);
            return;
        }

        if (Constants.COURSE_TYPE_SOFTWARE.equalsIgnoreCase(courseType)) {
            crawlerSupport.enterDepartmentCourse(session.driver(), session.driverWait(), Constants.SOFTWARE_COURSE);
            return;
        }

        throw new IllegalArgumentException("학과 공지인데 courseType 이 올바르지 않습니다: " + courseType);
    }

    private void waitUntilDetailLoaded(LmsCrawlSession session, String detailUrl) {
        try {
            if (isDepartmentNotice(detailUrl)) {
                session.driverWait().until(ExpectedConditions.or(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.bbsview")),
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector("td.textviewer")),
                        ExpectedConditions.presenceOfElementLocated(By.xpath("//th[normalize-space()='제목']"))
                ));
                return;
            }

            if (isCommunityNotice(detailUrl)) {
                session.driverWait().until(ExpectedConditions.or(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.artl_title")),
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(".artl_title")),
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.artl_content")),
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.artl_content_box")),
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.textviewer"))
                ));
                return;
            }

            session.driverWait().until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        } catch (TimeoutException e) {
            log.error("LMS 상세 페이지 핵심 요소 로딩 대기 실패. currentUrl={}", session.driver().getCurrentUrl(), e);
            throw e;
        }
    }

    private void dismissAlertIfPresent(LmsCrawlSession session) {
        try {
            Alert alert = session.driverWait().until(ExpectedConditions.alertIsPresent());
            String alertText = alert.getText();
            log.warn("LMS alert 감지: {}", alertText);
            alert.accept();

            if (alertText != null && alertText.contains("접근 권한이 없습니다")) {
                throw new RuntimeException("해당 과목에 접근 권한이 없습니다.");
            }
        } catch (TimeoutException ignored) {
        }
    }

}
