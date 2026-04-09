package org.example.noticeSummary.crawler.school;

import lombok.RequiredArgsConstructor;
import org.example.noticeSummary.ai.NoticeDetailAiService;
import org.example.noticeSummary.common.Constants;
import org.example.noticeSummary.dto.NoticeDetailResponse;
import org.example.noticeSummary.parser.model.ParsedNoticeDetail;
import org.example.noticeSummary.parser.school.SchoolNoticeDetailParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SchoolNoticeDetailCrawler {

    private final SchoolNoticeDetailParser schoolNoticeDetailParser;
    private final NoticeDetailAiService noticeDetailAiService;

    public NoticeDetailResponse crawlNoticeDetail(String id) {
        String detailUrl = Constants.ALL_BASE_URL + "/bbs/skhu/26/" + id + "/artclView.do";

        try {
            Document document = Jsoup.connect(detailUrl)
                    .userAgent(Constants.USER_AGENT)
                    .get();

            ParsedNoticeDetail parsed = schoolNoticeDetailParser.parse(id, detailUrl, document);
            return noticeDetailAiService.createNoticeDetailResponse(parsed);

        } catch (IOException e) {
            throw new RuntimeException("공지 상세 크롤링 실패", e);
        }
    }
}
