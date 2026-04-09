package org.example.noticeSummary.repository;

import org.example.noticeSummary.domain.Notice;
import org.example.noticeSummary.domain.NoticeSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Optional<Notice> findBySourceAndExternalId(NoticeSource source, String externalId);

    List<Notice> findTop50ByOrderByPublishedAtDescIdDesc();
}
