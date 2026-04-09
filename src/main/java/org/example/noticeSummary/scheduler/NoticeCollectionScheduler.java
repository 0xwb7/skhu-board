package org.example.noticeSummary.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.noticeSummary.application.NoticeCollectionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.crawl", name = "schedule-enabled", havingValue = "true")
public class NoticeCollectionScheduler {

    private static final Duration LOCK_LEASE_TIME = Duration.ofMinutes(30);

    private final NoticeCollectionService noticeCollectionService;
    private final ScheduledJobLockService scheduledJobLockService;

    @Scheduled(initialDelay = 10_000, fixedDelay = 120 * 60 * 1000)
    public void collectSchoolNotices() {
        scheduledJobLockService.runWithLock("collectSchoolNotices", LOCK_LEASE_TIME, () -> {
            log.info("학교 공지 수집 시작");
            noticeCollectionService.collectSchoolNotices();
            log.info("학교 공지 수집 종료");
        });
    }

    @Scheduled(initialDelay = 20_000, fixedDelay = 120 * 60 * 1000)
    public void collectLmsAllNotices() {
        scheduledJobLockService.runWithLock("collectLmsAllNotices", LOCK_LEASE_TIME, () -> {
            log.info("LMS 전체 공지 수집 시작");
            noticeCollectionService.collectLmsAllNotices();
            log.info("LMS 전체 공지 수집 종료");
        });
    }

    @Scheduled(initialDelay = 30_000, fixedDelay = 120 * 60 * 1000)
    public void collectDepartmentNotices() {
        scheduledJobLockService.runWithLock("collectDepartmentNotices", LOCK_LEASE_TIME, () -> {
            log.info("학과 LMS 공지 수집 시작");
            noticeCollectionService.collectItDepartmentNotices();
            noticeCollectionService.collectSoftwareDepartmentNotices();
            log.info("학과 LMS 공지 수집 종료");
        });
    }
}
