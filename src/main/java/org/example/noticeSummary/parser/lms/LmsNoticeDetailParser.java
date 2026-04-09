package org.example.noticeSummary.parser.lms;

import lombok.RequiredArgsConstructor;
import org.example.noticeSummary.common.Constants;
import org.example.noticeSummary.domain.NoticeSource;
import org.example.noticeSummary.parser.model.ParsedNoticeDetail;
import org.example.noticeSummary.util.CrawlerSupport;
import org.example.noticeSummary.util.LmsDateExtractor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LmsNoticeDetailParser {

    private final CrawlerSupport crawlerSupport;
    private final LmsDateExtractor lmsDateExtractor;

    public ParsedNoticeDetail parse(Document doc, String detailUrl, String courseType) {
        String articleId = extractArticleId(detailUrl, doc);
        String title = extractTitle(doc, detailUrl);
        String date = extractDate(doc, detailUrl);
        String content = extractContent(doc, detailUrl);
        boolean hasImage = hasMeaningfulImage(doc, detailUrl);

        if (title.isBlank()) {
            throw new RuntimeException("LMS 상세 공지 파싱 실패");
        }

        if (content.isBlank() && hasImage) {
            content = Constants.IMAGE_ONLY_NOTICE_MESSAGE;
        }

        if (content.isBlank()) {
            throw new RuntimeException("LMS 상세 공지 파싱 실패");
        }

        return new ParsedNoticeDetail(
                articleId,
                title,
                Constants.LMS_NOTICE_LABEL,
                date,
                content,
                detailUrl,
                resolveSource(detailUrl, courseType).name()
        );
    }

    public boolean isDepartmentNotice(String detailUrl) {
        return detailUrl != null && detailUrl.contains("/ilos/st/course/notice_view_form.acl");
    }

    public boolean isCommunityNotice(String detailUrl) {
        return detailUrl != null && detailUrl.contains("/ilos/community/notice_view_form.acl");
    }

    public NoticeSource resolveSource(String detailUrl, String courseType) {
        if (!isDepartmentNotice(detailUrl)) {
            return NoticeSource.LMS_ALL;
        }

        if (Constants.COURSE_TYPE_IT.equalsIgnoreCase(courseType)) {
            return NoticeSource.LMS_IT;
        }

        return NoticeSource.LMS_SOFTWARE;
    }

    private boolean hasMeaningfulImage(Document doc, String detailUrl) {
        Element root = isDepartmentNotice(detailUrl)
                ? doc.selectFirst("table.bbsview td.textviewer")
                : doc.selectFirst("div.artl_content_form.textviewer, div.artl_content_form, div.textviewer, td.textviewer");

        return root != null && !root.select("img").isEmpty();
    }

    private String extractArticleId(String detailUrl, Document doc) {
        String id = crawlerSupport.extractArticleId(detailUrl);
        if (id != null && !id.isBlank()) {
            return id;
        }

        Element hiddenInput = doc.selectFirst("input[name=ARTL_NUM]");
        if (hiddenInput != null) {
            String value = hiddenInput.attr("value").trim();
            if (!value.isBlank()) {
                return value;
            }
        }

        return "";
    }

    private String extractTitle(Document doc, String detailUrl) {
        return isDepartmentNotice(detailUrl) ? extractDepartmentTitle(doc) : extractCommunityTitle(doc);
    }

    private String extractDate(Document doc, String detailUrl) {
        return isDepartmentNotice(detailUrl) ? extractDepartmentDate(doc) : extractCommunityDate(doc);
    }

    private String extractContent(Document doc, String detailUrl) {
        return isDepartmentNotice(detailUrl) ? extractDepartmentContent(doc) : extractCommunityContent(doc);
    }

    private String extractDepartmentTitle(Document doc) {
        Elements rows = doc.select("table.bbsview tr");

        for (Element row : rows) {
            Element th = row.selectFirst("th");
            Element td = row.selectFirst("td");

            if (th == null || td == null) {
                continue;
            }

            String label = normalizeText(th.text());
            if ("제목".equals(label)) {
                String text = normalizeText(td.text());
                if (!text.isBlank()) {
                    return text;
                }
            }
        }

        Element fallback = doc.selectFirst("table.bbsview td.first.impt-wrap");
        return fallback != null ? normalizeText(fallback.text()) : "";
    }

    private String extractDepartmentDate(Document doc) {
        Elements rows = doc.select("table.bbsview tr");

        for (Element row : rows) {
            Element th = row.selectFirst("th");
            Element td = row.selectFirst("td");

            if (th == null || td == null) {
                continue;
            }

            String label = normalizeText(th.text());
            if ("게시일".equals(label)) {
                String extracted = lmsDateExtractor.extractOnlyDateText(td.text());
                return extracted.isBlank() ? normalizeText(td.text()) : extracted;
            }
        }

        return "";
    }

    private String extractDepartmentContent(Document doc) {
        Element contentElement = doc.selectFirst("table.bbsview td.textviewer");
        if (contentElement == null) {
            return "";
        }

        List<String> lines = new ArrayList<>();
        Elements blocks = contentElement.select("p, li");

        for (Element block : blocks) {
            String text = normalizeText(block.text());
            if (!text.isBlank()) {
                lines.add(text);
            }
        }

        return lines.isEmpty() ? normalizeText(contentElement.text()) : String.join("\n", lines);
    }

    private String extractCommunityTitle(Document doc) {
        String[] selectors = {
                "div.artl_title",
                ".artl_title",
                "h3.artl_title",
                "h4.artl_title",
                ".board-view-title",
                ".view-title"
        };

        for (String selector : selectors) {
            Element el = doc.selectFirst(selector);
            if (el != null) {
                String text = normalizeText(el.text());
                if (!text.isBlank()) {
                    return text;
                }
            }
        }

        return "";
    }

    private String extractCommunityDate(Document doc) {
        Element info = doc.selectFirst("div.artl_reg_info");
        if (info != null) {
            String extracted = lmsDateExtractor.extractOnlyDateText(info.text());
            if (!extracted.isBlank()) {
                return extracted;
            }
        }

        String[] selectors = {
                "td.bbs_date",
                ".bbs_date",
                "span.date",
                ".date",
                ".board-date",
                ".view-date"
        };

        for (String selector : selectors) {
            Elements elements = doc.select(selector);
            for (Element element : elements) {
                String extracted = lmsDateExtractor.extractOnlyDateText(element.text());
                if (!extracted.isBlank()) {
                    return extracted;
                }
            }
        }

        return "";
    }

    private String extractCommunityContent(Document doc) {
        String[] selectors = {
                "div.artl_content_form.textviewer",
                "td.textviewer",
                "div.textviewer",
                "div.artl_content_box .textviewer",
                "div.artl_content_box",
                "div.artl_content"
        };

        for (String selector : selectors) {
            Element el = doc.selectFirst(selector);
            if (el == null) {
                continue;
            }

            String parsed = extractMeaningfulContent(el);
            if (!parsed.isBlank()) {
                return parsed;
            }
        }

        return "";
    }

    private String extractMeaningfulContent(Element root) {
        List<String> lines = new ArrayList<>();

        Elements blocks = root.select("p, li");
        for (Element block : blocks) {
            String text = normalizeText(block.text());
            if (isMeaningfulLine(text)) {
                lines.add(text);
            }
        }

        if (!lines.isEmpty()) {
            return String.join("\n", lines);
        }

        String fallback = normalizeText(root.ownText());
        return isMeaningfulLine(fallback) ? fallback : "";
    }

    private boolean isMeaningfulLine(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String normalized = text.trim();

        return !normalized.contains("로그아웃")
                && !normalized.contains("Back to the top")
                && !normalized.contains("주 메뉴")
                && !normalized.contains("교육현황")
                && !normalized.contains("개설과목검색")
                && !normalized.contains("학사일정")
                && !normalized.contains("커뮤니티")
                && !normalized.contains("COPYRIGHT")
                && !normalized.contains("개인정보처리방침")
                && !normalized.startsWith("https://")
                && normalized.length() >= 2;
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
