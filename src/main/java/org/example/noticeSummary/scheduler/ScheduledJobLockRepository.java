package org.example.noticeSummary.scheduler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ScheduledJobLockRepository extends JpaRepository<ScheduledJobLock, String> {

    @Modifying
    @Query("""
            update ScheduledJobLock lock
               set lock.lockedUntil = :lockedUntil,
                   lock.lockedAt = :lockedAt,
                   lock.lockedBy = :lockedBy
             where lock.lockName = :lockName
               and lock.lockedUntil <= :now
            """)
    int acquire(
            @Param("lockName") String lockName,
            @Param("now") LocalDateTime now,
            @Param("lockedUntil") LocalDateTime lockedUntil,
            @Param("lockedAt") LocalDateTime lockedAt,
            @Param("lockedBy") String lockedBy
    );

    @Modifying
    @Query("""
            update ScheduledJobLock lock
               set lock.lockedUntil = :lockedUntil
             where lock.lockName = :lockName
               and lock.lockedBy = :lockedBy
            """)
    int release(
            @Param("lockName") String lockName,
            @Param("lockedUntil") LocalDateTime lockedUntil,
            @Param("lockedBy") String lockedBy
    );
}
