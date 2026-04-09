package org.example.noticeSummary.application;

import lombok.RequiredArgsConstructor;
import org.example.noticeSummary.dto.NoticeDetailResponse;
import org.example.noticeSummary.dto.NoticeResponse;
import org.example.noticeSummary.mapper.NoticeResponseMapper;
import org.example.noticeSummary.repository.NoticeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeReadService {

    private final NoticeRepository noticeRepository;
    private final NoticeResponseMapper noticeResponseMapper;

    public List<NoticeResponse> getRecentNotices() {
        return noticeRepository.findTop50ByOrderByPublishedAtDescIdDesc()
                .stream()
                .map(noticeResponseMapper::toNoticeResponse)
                .toList();
    }

    public NoticeDetailResponse getNoticeDetail(Long id) {
        var notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항이 존재하지 않습니다."));

        return noticeResponseMapper.toNoticeDetailResponse(notice);
    }
}
