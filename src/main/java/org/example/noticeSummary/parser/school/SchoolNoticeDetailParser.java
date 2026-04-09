package org.example.noticeSummary.parser.school;

import org.example.noticeSummary.common.Constants;
import org.example.noticeSummary.domain.NoticeSource;
import org.example.noticeSummary.parser.model.ParsedNoticeDetail;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SchoolNoticeDetailParser {

    public ParsedNoticeDetail parse(String id, String detailUrl, Document document) {
        String title = extractTitle(document);
        String date = extractDate(document);
        String content = extractContent(document);
        boolean hasImage = hasMeaningfulImage(document);

        if (title.isBlank()) {
            throw new RuntimeException("상세 제목 추출 실패");
        }

        if (content.isBlank() && hasImage) {
            content = Constants.IMAGE_ONLY_NOTICE_MESSAGE;
        }

        if (content.isBlank()) {
            content = "본문을 불러오지 못했습니다.";
        }

        return new ParsedNoticeDetail(
                id,
                title,
                Constants.SCHOOL_NOTICE_LABEL,
                date,
                content,
                detailUrl,
                NoticeSource.SCHOOL.name()
        );
    }

    private boolean hasMeaningfulImage(Document document) {
        Element viewCon = document.selectFirst("div.view-con");
        return viewCon != null && !viewCon.select("img").isEmpty();
    }

    private String extractTitle(Document document) {
        Element titleElement = document.selectFirst("h2.view-title");
        return titleElement != null ? titleElement.text().trim() : "";
    }

    private String extractDate(Document document) {
        Element dateElement = document.selectFirst("div.view-detail dl.write dd");
        return dateElement != null ? dateElement.text().trim() : "";
    }

    private String extractContent(Document document) {
        Element viewCon = document.selectFirst("div.view-con");
        if (viewCon == null) {
            return "";
        }

        List<String> lines = new ArrayList<>();
        Elements paragraphs = viewCon.select("p");

        for (Element p : paragraphs) {
            String text = p.text()
                    .replace("\u00A0", " ")
                    .trim();

            if (!text.isBlank()) {
                lines.add(text);
            }
        }

        if (!lines.isEmpty()) {
            return String.join("\n", lines);
        }

        return viewCon.text()
                .replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
