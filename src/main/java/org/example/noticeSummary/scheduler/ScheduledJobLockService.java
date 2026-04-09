package org.example.noticeSummary.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledJobLockService {

    private final ScheduledJobLockRepository scheduledJobLockRepository;
    private final PlatformTransactionManager transactionManager;

    public void runWithLock(String lockName, Duration leaseTime, Runnable task) {
        String lockedBy = resolveLockedBy();

        if (!tryAcquire(lockName, leaseTime, lockedBy)) {
            log.info("스케줄 실행 생략. lockName={}", lockName);
            return;
        }

        try {
            task.run();
        } finally {
            release(lockName, lockedBy);
        }
    }

    protected boolean tryAcquire(String lockName, Duration leaseTime, String lockedBy) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setReadOnly(false);

        Boolean acquired = transactionTemplate.execute(status -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lockedUntil = now.plus(leaseTime);

            int updated = scheduledJobLockRepository.acquire(lockName, now, lockedUntil, now, lockedBy);
            if (updated > 0) {
                return true;
            }

            try {
                scheduledJobLockRepository.save(ScheduledJobLock.builder()
                        .lockName(lockName)
                        .lockedAt(now)
                        .lockedUntil(lockedUntil)
                        .lockedBy(lockedBy)
                        .build());
                return true;
            } catch (DataIntegrityViolationException ignored) {
                return false;
            }
        });

        return Boolean.TRUE.equals(acquired);
    }

    protected void release(String lockName, String lockedBy) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setReadOnly(false);
        transactionTemplate.executeWithoutResult(status ->
                scheduledJobLockRepository.release(lockName, LocalDateTime.now(), lockedBy)
        );
    }

    private String resolveLockedBy() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
            return "unknown-host";
        }
    }
}
