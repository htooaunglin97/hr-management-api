package com.example.hr.attendance.repository;

import com.example.hr.attendance.entity.Attendance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class AttendanceRepositoryTest {

    @Autowired
    private AttendanceRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testExistsByEmployeeIdAndCheckInBetween() {

        UUID empId = UUID.randomUUID();

        Instant startOfToday = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Instant endOfToday = startOfToday.plus(Duration.ofDays(1)).minusNanos(1);

        Attendance yesterdayRecord = new Attendance();
        yesterdayRecord.setEmployeeId(empId);
        yesterdayRecord.setCheckIn(startOfToday.minus(Duration.ofHours(5)));
        entityManager.persist(yesterdayRecord);

        Attendance todayRecord = new Attendance();
        todayRecord.setEmployeeId(empId);
        todayRecord.setCheckIn(startOfToday.plus(Duration.ofHours(2)));
        entityManager.persist(todayRecord);

        entityManager.flush();

        boolean existsToday = repository.existsByEmployeeIdAndCheckInBetween(empId, startOfToday, endOfToday);
        boolean existsTomorrow = repository.existsByEmployeeIdAndCheckInBetween(empId, endOfToday.plusNanos(1), endOfToday.plus(Duration.ofDays(1)));

        assertThat(existsToday).isTrue();
        assertThat(existsTomorrow).isFalse();

    }

}
