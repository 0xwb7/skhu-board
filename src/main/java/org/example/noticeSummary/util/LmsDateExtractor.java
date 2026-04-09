package org.example.noticeSummary.util;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LmsDateExtractor {

    private static final Pattern[] DATE_PATTERNS = {
            Pattern.compile("\\d{4}\\.\\d{2}\\.\\d{2}(\\s+(오전|오후)\\s+\\d{1,2}:\\d{2}(:\\d{2})?)?"),
            Pattern.compile("\\d{4}-\\d{2}-\\d{2}(\\s+\\d{1,2}:\\d{2}(:\\d{2})?)?"),
            Pattern.compile("\\d{2}\\.\\d{2}(\\s+(오전|오후)\\s+\\d{1,2}:\\d{2})?"),
            Pattern.compile("\\d+시간 전"),
            Pattern.compile("\\d+분 전"),
            Pattern.compile("\\d+일 전(\\s+(오전|오후)\\s+\\d{1,2}:\\d{2})?"),
            Pattern.compile("어제(\\s+(오전|오후)\\s+\\d{1,2}:\\d{2})?")
    };

    public String extractOnlyDateText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String normalized = text.replaceAll("\\s+", " ").trim();

        for (Pattern pattern : DATE_PATTERNS) {
            Matcher matcher = pattern.matcher(normalized);
            if (matcher.find()) {
                return matcher.group();
            }
        }

        return "";
    }

    public String extractDateFromCells(Element row) {
        Elements cells = row.select("td");

        for (int i = cells.size() - 1; i >= 0; i--) {
            String extracted = extractOnlyDateText(cells.get(i).text());
            if (!extracted.isBlank()) {
                return extracted;
            }
        }

        return "";
    }
}
