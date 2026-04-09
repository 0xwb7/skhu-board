package org.example.noticeSummary.crawler.lms;

import lombok.RequiredArgsConstructor;
import org.example.noticeSummary.common.Constants;
import org.example.noticeSummary.domain.NoticeSource;
import org.example.noticeSummary.dto.NoticeResponse;
import org.example.noticeSummary.util.LmsDateExtractor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LmsAllNoticeListCrawler {

    private final LmsSessionFactory lmsSessionFactory;
    private final LmsDateExtractor lmsDateExtractor;

    private static final String NOTICE_LIST_URL = "https://lms.skhu.ac.kr/ilos/community/notice_list_form.acl";

    public List<NoticeResponse> crawlNotices() {
        try (LmsCrawlSession session = lmsSessionFactory.createLoggedInSession()) {
            return crawlNotices(session);
        }
    }

    public List<NoticeResponse> crawlNotices(LmsCrawlSession session) {
        try {
            session.driver().get(NOTICE_LIST_URL);

            session.driverWait().until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("table tbody tr")
            ));

            String html = session.driver().getPageSource();
            Document doc = Jsoup.parse(html);

            Elements rows = doc.select("table tbody tr");
            List<NoticeResponse> notices = new ArrayList<>();

            for (Element row : rows) {
                Element titleLink = row.selectFirst("a[href*='notice_view_form.acl']");
                if (titleLink == null) {
                    continue;
                }

                String title = titleLink.text()
                        .replaceAll("\\s+", " ")
                        .trim();

                if (title.isBlank()) {
                    continue;
                }

                String href = titleLink.attr("href").trim();
                String url = href.startsWith("http") ? href : Constants.LMS_BASE_URL + href;

                String date = lmsDateExtractor.extractDateFromCells(row);
                String id = extractArticleId(url, notices.size());

                notices.add(new NoticeResponse(
                        id,
                        title,
                        "",
                        Constants.LMS_ALL_NOTICE_LABEL,
                        date,
                        url,
                        NoticeSource.LMS_ALL.name()
                ));

                if (notices.size() >= 10) {
                    break;
                }
            }

            return notices;
        } catch (Exception e) {
            throw new RuntimeException("LMS 공지 목록 크롤링 실패", e);
        }
    }

    private String extractArticleId(String url, int currentSize) {
        String[] split = url.split("ARTL_NUM=");
        if (split.length > 1) {
            return split[1].split("&")[0];
        }
        return String.valueOf(currentSize + 1);
    }
}
