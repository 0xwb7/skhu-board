package org.example.noticeSummary.support;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class NoticePublishedAtParser {

    public LocalDateTime parsePublishedAt(String displayDate) {
        if (displayDate == null || displayDate.isBlank()) {
            return LocalDateTime.now();
        }

        String text = displayDate.trim().replaceAll("\\s+", " ");

        LocalDateTime parsed = parseFullKoreanDateTime(text);
        if (parsed != null) {
            return parsed;
        }

        parsed = parseShortKoreanDateTime(text);
        if (parsed != null) {
            return parsed;
        }

        parsed = parseRelativeDateTime(text);
        if (parsed != null) {
            return parsed;
        }

        parsed = parseDateOnly(text);
        if (parsed != null) {
            return parsed;
        }

        return LocalDateTime.now();
    }

    private LocalDateTime parseFullKoreanDateTime(String text) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd a h:mm")
                    .withLocale(java.util.Locale.KOREAN);
            return LocalDateTime.parse(text, formatter);
        } catch (Exception ignored) {
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd a h:mm:ss")
                    .withLocale(java.util.Locale.KOREAN);
            return LocalDateTime.parse(text, formatter);
        } catch (Exception ignored) {
        }

        return null;
    }

    private LocalDateTime parseShortKoreanDateTime(String text) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.dd a h:mm")
                    .withLocale(java.util.Locale.KOREAN);

            var parsed = formatter.parse(text);
            int month = parsed.get(java.time.temporal.ChronoField.MONTH_OF_YEAR);
            int day = parsed.get(java.time.temporal.ChronoField.DAY_OF_MONTH);
            int hour = parsed.get(java.time.temporal.ChronoField.HOUR_OF_AMPM);
            int ampm = parsed.get(java.time.temporal.ChronoField.AMPM_OF_DAY);
            int minute = parsed.get(java.time.temporal.ChronoField.MINUTE_OF_HOUR);

            int finalHour = (ampm == 1 ? 12 : 0) + hour;
            if (finalHour == 24) {
                finalHour = 12;
            }

            LocalDate now = LocalDate.now();
            return LocalDateTime.of(now.getYear(), month, day, finalHour, minute);
        } catch (Exception ignored) {
        }

        return null;
    }

    private LocalDateTime parseRelativeDateTime(String text) {
        LocalDateTime now = LocalDateTime.now();

        try {
            if (text.matches("\\d+분 전")) {
                int minutes = Integer.parseInt(text.replace("분 전", "").trim());
                return now.minusMinutes(minutes);
            }

            if (text.matches("\\d+시간 전")) {
                int hours = Integer.parseInt(text.replace("시간 전", "").trim());
                return now.minusHours(hours);
            }

            if (text.matches("\\d+일 전( (오전|오후) \\d{1,2}:\\d{2})?")) {
                String daysText = text.split("일 전")[0].trim();
                int days = Integer.parseInt(daysText);
                LocalDateTime base = now.minusDays(days);

                if (text.contains("오전") || text.contains("오후")) {
                    LocalTime time = parseKoreanTime(text);
                    if (time != null) {
                        return LocalDateTime.of(base.toLocalDate(), time);
                    }
                }

                return base;
            }

            if (text.matches("어제( (오전|오후) \\d{1,2}:\\d{2})?")) {
                LocalDate date = now.toLocalDate().minusDays(1);

                if (text.contains("오전") || text.contains("오후")) {
                    LocalTime time = parseKoreanTime(text);
                    if (time != null) {
                        return LocalDateTime.of(date, time);
                    }
                }

                return date.atStartOfDay();
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private LocalDateTime parseDateOnly(String text) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            return LocalDate.parse(text, formatter).atStartOfDay();
        } catch (Exception ignored) {
        }

        return null;
    }

    private LocalTime parseKoreanTime(String text) {
        try {
            String normalized = text.replaceAll("^.*?(오전|오후)", "$1").trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h:mm")
                    .withLocale(java.util.Locale.KOREAN);
            return LocalTime.parse(normalized, formatter);
        } catch (Exception ignored) {
        }

        return null;
    }
}

