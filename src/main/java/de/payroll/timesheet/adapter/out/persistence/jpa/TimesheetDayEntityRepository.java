package de.payroll.timesheet.adapter.out.persistence.jpa;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimesheetDayEntityRepository extends JpaRepository<TimesheetDayEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
            SELECT d FROM TimesheetDayEntity d
            LEFT JOIN FETCH d.intervals
            WHERE d.companyId = :companyId
              AND d.employeeId = :employeeId
              AND d.workDate IN :dates
            ORDER BY d.workDate
            """)
    List<TimesheetDayEntity> findForUpdateByWorkDates(
            @Param("companyId") String companyId,
            @Param("employeeId") String employeeId,
            @Param("dates") Collection<LocalDate> dates);
}
