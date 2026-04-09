package org.example.noticeSummary.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.noticeSummary.common.Constants;
import org.example.noticeSummary.domain.NoticeCategory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OpenAiNoticeClient {

    @Value("${openai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiNoticeAnalysis analyzeNotice(String title, String content) {
        String categoryOptions = Arrays.stream(NoticeCategory.values())
                .map(NoticeCategory::label)
                .collect(Collectors.joining(", "));

        String prompt = """
                다음 대학 공지사항의 제목과 본문을 보고 아래 JSON 형식으로만 응답해줘.
                설명 문장, 코드블록, 마크다운 없이 JSON 객체만 출력해.

                JSON 형식:
                {
                  "category": "카테고리명",
                  "summary": "2~3문장 요약",
                  "organizedContent": "읽기 쉽게 정리한 본문"
                }

                조건:
                - category는 아래 카테고리 목록 중 하나만 사용해.
                - summary는 학생이 빠르게 이해할 수 있도록 2~3문장으로 짧고 자연스럽게 작성해.
                - organizedContent는 대상, 일정 또는 기간, 해야 할 일, 주의사항 위주로 핵심만 정리해.
                - summary와 organizedContent는 md 문법 없이 plain text로 작성해.
                - 본문에 이미지가 있으면 summary와 organizedContent 첫 줄에 원본 사이트에서 이미지를 확인하라고 안내해.
                - 없는 내용은 억지로 만들지 마.

                카테고리 목록:
                """ + categoryOptions + """

                제목:
                """ + safeText(title) + """

                본문:
                """ + limitContent(content);

        String responseText = callOpenAi(prompt).trim();
        return parseAnalysis(responseText);
    }

    private String safeText(String text) {
        return text == null ? "" : text.trim();
    }

    private String limitContent(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        if (content.length() > 3000) {
            return content.substring(0, 3000);
        }

        return content;
    }

    private String callOpenAi(String prompt) {
        if (prompt.isBlank()) {
            return Constants.AI_EMPTY_PROMPT_MESSAGE;
        }

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4.1-mini",
                "input", prompt
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity =
                new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.openai.com/v1/responses",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            Map responseBody = response.getBody();
            if (responseBody == null) {
                return Constants.AI_FAILURE_MESSAGE;
            }

            Object outputText = responseBody.get("output_text");
            if (outputText instanceof String text && !text.isBlank()) {
                return text.trim();
            }

            Object outputObj = responseBody.get("output");
            if (outputObj instanceof List<?> outputList && !outputList.isEmpty()) {
                Object firstOutput = outputList.get(0);

                if (firstOutput instanceof Map<?, ?> outputMap) {
                    Object contentObj = outputMap.get("content");

                    if (contentObj instanceof List<?> contentList && !contentList.isEmpty()) {
                        Object firstContent = contentList.get(0);

                        if (firstContent instanceof Map<?, ?> contentMap) {
                            Object textObj = contentMap.get("text");

                            if (textObj instanceof String text && !text.isBlank()) {
                                return text.trim();
                            }
                        }
                    }
                }
            }

            return Constants.AI_FAILURE_MESSAGE;
        } catch (Exception e) {
            log.warn("OpenAI 호출 실패", e);
            return Constants.AI_FAILURE_MESSAGE;
        }
    }

    private AiNoticeAnalysis parseAnalysis(String responseText) {
        if (responseText == null || responseText.isBlank() || Constants.AI_FAILURE_MESSAGE.equals(responseText)) {
            return failureAnalysis();
        }

        String jsonText = extractJsonObject(responseText);
        if (jsonText == null) {
            return failureAnalysis();
        }

        try {
            AiNoticeAnalysisPayload payload = objectMapper.readValue(jsonText, AiNoticeAnalysisPayload.class);

            NoticeCategory category = NoticeCategory.fromLabel(payload.category());
            String summary = normalizeAiText(payload.summary(), Constants.AI_SUMMARY_FALLBACK_MESSAGE);
            String organizedContent = normalizeAiText(payload.organizedContent(), Constants.AI_ORGANIZED_CONTENT_FALLBACK_MESSAGE);

            return new AiNoticeAnalysis(true, category, summary, organizedContent);
        } catch (Exception e) {
            log.warn("OpenAI 응답 파싱 실패", e);
            return failureAnalysis();
        }
    }

    private AiNoticeAnalysis failureAnalysis() {
        return new AiNoticeAnalysis(
                false,
                NoticeCategory.ETC,
                Constants.AI_SUMMARY_FALLBACK_MESSAGE,
                Constants.AI_ORGANIZED_CONTENT_FALLBACK_MESSAGE
        );
    }

    private String normalizeAiText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end < start) {
            return null;
        }
        return text.substring(start, end + 1);
    }
}
