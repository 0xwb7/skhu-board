package org.example.noticeSummary.domain;

import java.util.Arrays;

public enum NoticeCategory {
    SCHOLARSHIP("장학"),
    RECRUITMENT("모집"),
    CONTEST("대회"),
    INTERNSHIP("인턴십"),
    SPECIAL_LECTURE("특강"),
    EVENT("행사"),
    ACADEMIC("학사"),
    CLASS("수업"),
    EMPLOYMENT("취업"),
    EXTRACURRICULAR("비교과"),
    ETC("기타");

    private final String label;

    NoticeCategory(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static NoticeCategory fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return ETC;
        }

        String normalized = value.trim();

        return switch (normalized) {
            case "장학" -> SCHOLARSHIP;
            case "모집" -> RECRUITMENT;
            case "대회", "경진대회", "공모전", "해커톤" -> CONTEST;
            case "인턴십" -> INTERNSHIP;
            case "특강" -> SPECIAL_LECTURE;
            case "행사", "설명회" -> EVENT;
            case "학사" -> ACADEMIC;
            case "수업", "강의" -> CLASS;
            case "취업" -> EMPLOYMENT;
            case "비교과", "교환학생", "봉사" -> EXTRACURRICULAR;
            case "인공지능전공", "IT융합자율학부", "소프트웨어공학전공", "기타" -> ETC;
            default -> Arrays.stream(values())
                    .filter(category -> category.label.equals(normalized))
                    .findFirst()
                    .orElse(ETC);
        };
    }
}
