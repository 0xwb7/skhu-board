package org.example.noticeSummary.crawler.lms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.noticeSummary.common.Constants;
import org.example.noticeSummary.domain.NoticeSource;
import org.example.noticeSummary.dto.NoticeResponse;
import org.example.noticeSummary.util.CrawlerSupport;
import org.example.noticeSummary.util.LmsDateExtractor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class LmsDepartmentNoticeListCrawler {

    private final LmsSessionFactory lmsSessionFactory;
    private final CrawlerSupport crawlerSupport;
    private final LmsDateExtractor lmsDateExtractor;

    public List<NoticeResponse> crawlItDepartmentNotices() {
        return crawlDepartmentNotices(Constants.IT_COURSE, Constants.LMS_IT_NOTICE_LABEL, NoticeSource.LMS_IT);
    }

    public List<NoticeResponse> crawlSoftwareDepartmentNotices() {
        return crawlDepartmentNotices(Constants.SOFTWARE_COURSE, Constants.LMS_SOFTWARE_NOTICE_LABEL, NoticeSource.LMS_SOFTWARE);
    }

    private List<NoticeResponse> crawlDepartmentNotices(String courseText, String category, NoticeSource source) {
        try (LmsCrawlSession session = lmsSessionFactory.createLoggedInSession()) {
            return crawlDepartmentNotices(session, courseText, category, source);
        }
    }

    public List<NoticeResponse> crawlDepartmentNotices(LmsCrawlSession session, String courseText, String category, NoticeSource source) {
        try {
            crawlerSupport.enterDepartmentCourse(session.driver(), session.driverWait(), courseText);

            String html = session.driver().getPageSource();
            Document doc = Jsoup.parse(html);

            Elements rows = doc.select("table tbody tr");
            List<NoticeResponse> notices = new ArrayList<>();

            for (Element row : rows) {
                Element titleCell = row.selectFirst("td[onclick*='notice_view_form.acl']");
                if (titleCell == null) {
                    continue;
                }

                String title = extractDepartmentTitle(titleCell);
                if (title.isBlank()) {
                    continue;
                }

                String onclick = titleCell.attr("onclick").trim();
                String url = extractUrlFromOnclick(onclick);
                if (url == null || url.isBlank()) {
                    continue;
                }

                if (!url.startsWith("http")) {
                    url = Constants.LMS_BASE_URL + url;
                }

                String date = extractDepartmentDate(row);

                String id = crawlerSupport.extractArticleId(url);
                if (id == null) {
                    id = String.valueOf(notices.size() + 1);
                }

                notices.add(new NoticeResponse(
                        id,
                        title,
                        "",
                        category,
                        date,
                        url,
                        source.name()
                ));

                if (notices.size() >= 10) {
                    break;
                }
            }

            return notices;
        } catch (Exception e) {
            log.error("{} 크롤링 실패", category, e);
            throw new RuntimeException(category + " 크롤링 실패", e);
        }
    }

    private String extractDepartmentTitle(Element titleCell) {
        Element titleDiv = titleCell.selectFirst("div.subjt_top");
        if (titleDiv != null) {
            return cleanTitle(titleDiv.text());
        }

        Element link = titleCell.selectFirst("a.site-link");
        if (link == null) {
            return "";
        }

        Elements divs = link.select("> div");
        for (Element div : divs) {
            if (!div.hasClass("subjt_bottom")) {
                String text = cleanTitle(div.text());
                if (!text.isBlank()) {
                    return text;
                }
            }
        }

        return cleanTitle(link.text());
    }

    private String cleanTitle(String rawTitle) {
        return rawTitle
                .replaceAll("\\s*\\[\\d+\\]\\s*", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String extractDepartmentDate(Element row) {
        Elements cells = row.select("td.number");

        for (Element cell : cells) {
            String extracted = lmsDateExtractor.extractOnlyDateText(cell.text());
            if (!extracted.isBlank()) {
                return extracted;
            }
        }

        return lmsDateExtractor.extractDateFromCells(row);
    }

    private String extractUrlFromOnclick(String onclick) {
        Pattern pattern = Pattern.compile("pageMove\\('([^']+)'\\)");
        Matcher matcher = pattern.matcher(onclick);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}
