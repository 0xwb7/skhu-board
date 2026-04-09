package org.example.noticeSummary.controller.api;

import org.example.noticeSummary.application.NoticeReadService;
import org.example.noticeSummary.dto.NoticeDetailResponse;
import org.example.noticeSummary.dto.NoticeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NoticeReadControllerTest {

    @Mock
    private NoticeReadService noticeReadService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        NoticeReadController controller = new NoticeReadController(noticeReadService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void getNoticesReturnsRecentNoticeList() throws Exception {
        given(noticeReadService.getRecentNotices()).willReturn(List.of(
                new NoticeResponse("1", "제목", "요약", "장학", "2026.04.08", "https://example.com/1", "SCHOOL")
        ));

        mockMvc.perform(get("/api/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].title").value("제목"));
    }

    @Test
    void getNoticeDetailReturnsNotFoundWhenNoticeDoesNotExist() throws Exception {
        given(noticeReadService.getNoticeDetail(999L))
                .willThrow(new IllegalArgumentException("공지사항이 존재하지 않습니다."));

        mockMvc.perform(get("/api/notices/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("공지사항이 존재하지 않습니다."));
    }

    @Test
    void getNoticeDetailReturnsAiGeneratedFlag() throws Exception {
        given(noticeReadService.getNoticeDetail(1L)).willReturn(
                new NoticeDetailResponse(
                        "1",
                        "제목",
                        "요약",
                        "정리",
                        true,
                        "장학",
                        "2026.04.08",
                        "본문",
                        "https://example.com/1",
                        "SCHOOL"
                )
        );

        mockMvc.perform(get("/api/notices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiGenerated").value(true));
    }
}
