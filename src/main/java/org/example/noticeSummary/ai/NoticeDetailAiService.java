package org.example.noticeSummary.ai;

import lombok.RequiredArgsConstructor;
import org.example.noticeSummary.dto.NoticeDetailResponse;
import org.example.noticeSummary.parser.model.ParsedNoticeDetail;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticeDetailAiService {

    private final OpenAiNoticeClient openAiNoticeClient;

    public NoticeDetailResponse createNoticeDetailResponse(ParsedNoticeDetail detail) {
        AiNoticeAnalysis analysis = openAiNoticeClient.analyzeNotice(detail.title(), detail.content());

        return new NoticeDetailResponse(
                detail.id(),
                detail.title(),
                analysis.summary(),
                analysis.organizedContent(),
                analysis.success(),
                analysis.category().label(),
                detail.date(),
                detail.content(),
                detail.url(),
                detail.source()
        );
    }
}
