package org.example.noticeSummary.common;

public final class Constants {

    private Constants() {
    }

    public static final String LMS_BASE_URL = "https://lms.skhu.ac.kr";
    public static final String ALL_BASE_URL = "https://www.skhu.ac.kr";
    public static final String USER_AGENT = "Mozilla/5.0";

    public static final String COURSE_TYPE_IT = "it";
    public static final String COURSE_TYPE_SOFTWARE = "software";
    public static final String IT_COURSE = "IT융합자율학부 어울림";
    public static final String SOFTWARE_COURSE = "소프트웨어공학전공생 어울림";

    public static final String SCHOOL_NOTICE_LABEL = "학부공지";
    public static final String LMS_NOTICE_LABEL = "LMS 공지";
    public static final String LMS_ALL_NOTICE_LABEL = "LMS 전체공지";
    public static final String LMS_IT_NOTICE_LABEL = "IT융합자율학부 공지";
    public static final String LMS_SOFTWARE_NOTICE_LABEL = "소프트웨어공학부 공지";

    public static final String IMAGE_ONLY_NOTICE_MESSAGE = "이미지만 있는 공지입니다. 원문 페이지에서 이미지를 확인해주세요.";
    public static final String AI_SUMMARY_FALLBACK_MESSAGE = "AI 요약을 생성하지 못했습니다.";
    public static final String AI_ORGANIZED_CONTENT_FALLBACK_MESSAGE = "AI가 상세 내용을 정리하지 못했습니다.";
    public static final String AI_EMPTY_PROMPT_MESSAGE = "내용이 없습니다.";
    public static final String AI_FAILURE_MESSAGE = "생성하지 못했습니다.";
}
