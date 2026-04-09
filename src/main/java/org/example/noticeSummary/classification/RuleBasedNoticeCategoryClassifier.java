package org.example.noticeSummary.classification;

import org.example.noticeSummary.domain.NoticeCategory;
import org.springframework.stereotype.Component;

@Component
public class RuleBasedNoticeCategoryClassifier {

    public NoticeCategory classify(String title, String content) {
        String text = ((title == null ? "" : title) + " " + (content == null ? "" : content)).toLowerCase();

        if (contains(text, "장학", "장학생")) {
            return NoticeCategory.SCHOLARSHIP;
        }
        if (contains(text, "경진대회", "공모전", "해커톤")) {
            return NoticeCategory.CONTEST;
        }
        if (contains(text, "인턴", "현장실습")) {
            return NoticeCategory.INTERNSHIP;
        }
        if (contains(text, "특강", "강연", "세미나")) {
            return NoticeCategory.SPECIAL_LECTURE;
        }
        if (contains(text, "행사", "축제", "프로그램", "설명회")) {
            return NoticeCategory.EVENT;
        }
        if (contains(text, "수강", "강의", "수업", "출결")) {
            return NoticeCategory.CLASS;
        }
        if (contains(text, "학사", "휴학", "복학", "졸업", "성적", "등록")) {
            return NoticeCategory.ACADEMIC;
        }
        if (contains(text, "취업", "채용", "직무", "진로")) {
            return NoticeCategory.EMPLOYMENT;
        }
        if (contains(text, "비교과", "마일리지", "교환학생", "파견", "봉사")) {
            return NoticeCategory.EXTRACURRICULAR;
        }
        if (contains(text, "모집", "선발", "신청", "접수")) {
            return NoticeCategory.RECRUITMENT;
        }

        return NoticeCategory.ETC;
    }

    private boolean contains(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
