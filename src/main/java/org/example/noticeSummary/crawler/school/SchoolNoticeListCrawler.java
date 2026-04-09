package org.example.noticeSummary.crawler.school;

import org.example.noticeSummary.common.Constants;
import org.example.noticeSummary.domain.NoticeSource;
import org.example.noticeSummary.dto.NoticeResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SchoolNoticeListCrawler {

    private static final String LIST_URL = "https://www.skhu.ac.kr/bbs/skhu/26/artclList.do";

    public List<NoticeResponse> crawlNotices() {
        List<NoticeResponse> notices = new ArrayList<>();

        try {
            Document document = Jsoup.connect(LIST_URL)
                    .userAgent(Constants.USER_AGENT)
                    .get();

            Elements rows = document.select("table.board-table tbody tr");

            for (Element row : rows) {
                Element titleLink = row.selectFirst("td.td-subject a");
                Element dateElement = row.selectFirst("td.td-date");

                if (titleLink == null || dateElement == null) {
                    continue;
                }

                String title = titleLink.text().trim();
                String relativeUrl = titleLink.attr("href").trim();
                String articleUrl = Constants.ALL_BASE_URL + relativeUrl;
                String date = dateElement.text().trim();

                String articleId = extractArticleId(relativeUrl);

                if (articleId == null) {
                    continue;
                }

                notices.add(new NoticeResponse(
                        articleId,
                        title,
                        "",
                        Constants.SCHOOL_NOTICE_LABEL,
                        date,
                        articleUrl,
                        NoticeSource.SCHOOL.name()
                ));

                if (notices.size() >= 10) {
                    break;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("공지 크롤링 실패", e);
        }

        return notices;
    }

    private String extractArticleId(String url) {
        Pattern pattern = Pattern.compile("/bbs/skhu/26/(\\d+)/artclView\\.do");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}
